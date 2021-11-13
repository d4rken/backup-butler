package eu.darken.bb.storage.core.saf

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.R
import eu.darken.bb.common.ApiHelper
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.saf.SAFGateway
import eu.darken.bb.common.files.core.saf.SAFPath
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.moshi.fromSAFFile
import eu.darken.bb.common.moshi.toSAFFile
import eu.darken.bb.storage.core.ExistingStorageException
import eu.darken.bb.storage.core.IllegalStoragePathException
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageEditor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SAFStorageEditor @AssistedInject constructor(
    @Assisted initialStorageId: Storage.Id,
    @ApplicationContext private val context: Context,
    moshi: Moshi,
    private val safGateway: SAFGateway,
    @AppScope private val appScope: CoroutineScope,
) : StorageEditor {

    private val editorDataPub = DynamicStateFlow(TAG, appScope) { Data(storageId = initialStorageId) }
    override val editorData = editorDataPub.flow

    private val configAdapter = moshi.adapter(SAFStorageConfig::class.java)

    private var originalRefPath: SAFPath? = null

    fun updateLabel(label: String) = editorDataPub.updateAsync { copy(label = label) }

    suspend fun updatePath(_path: SAFPath, importExisting: Boolean, isNewlyPersisted: Boolean? = null): SAFPath {
        check(!editorDataPub.value().existingStorage) { "Can't change path on an existing storage." }

        check(_path.canWrite(safGateway)) { "Got permissions, but can't write to the path." }
        check(_path.isDirectory(safGateway)) { "Target is not a directory!" }

        val isStorageRoot = safGateway.isStorageRoot(_path)
        if (isStorageRoot && ApiHelper.hasAndroid11()) {
            throw IllegalStoragePathException(_path, R.string.storage_saf_error_cant_pick_root)
        }

        val tweakedPath: SAFPath = if (isStorageRoot) {
            _path.child("BackupButler")
        } else {
            _path
        }

        if (tweakedPath.child(STORAGE_CONFIG).exists(safGateway)) {
            if (!importExisting) throw ExistingStorageException(tweakedPath)

            load(tweakedPath)
        } else {
            editorDataPub.updateBlocking {
                copy(
                    label = if (this.label.isEmpty()) tweakedPath.userReadableName(context) else this.label,
                    refPath = tweakedPath,
                    pathIsNewlyPersisted = isNewlyPersisted ?: this.pathIsNewlyPersisted
                )
            }
        }

        return tweakedPath
    }

    override fun isValid(): Flow<Boolean> = editorData.map {
        it.refPath != null && it.label.isNotEmpty()
    }

    override suspend fun load(ref: Storage.Ref): SAFStorageConfig {
        ref as SAFStorageRef
        return load(ref.path)
    }

    private suspend fun load(path: SAFPath): SAFStorageConfig {
        val config = configAdapter.fromSAFFile(safGateway, path.child(STORAGE_CONFIG))

        editorDataPub.updateBlocking {
            copy(
                refPath = path,
                existingStorage = true,
                storageId = config.storageId,
                label = config.label
            )
        }

        return config
    }

    override suspend fun save(): Pair<Storage.Ref, Storage.Config> {
        Timber.tag(TAG).v("save()")
        val saved = editorDataPub.updateBlocking {
            this.refPath as SAFPath

            if (this.refPath != originalRefPath) {
                originalRefPath?.let { safGateway.releasePermission(it) }
            }

            require(safGateway.hasPermission(this.refPath)) { "No permission persisted for ${this.refPath}?!" }

            copy(
                pathIsNewlyPersisted = false
            )
        }

        saved.refPath as SAFPath

        val ref = SAFStorageRef(
            storageId = saved.storageId,
            path = saved.refPath
        )
        val config = SAFStorageConfig(
            storageId = saved.storageId,
            label = saved.label
        )

        val configFile = ref.path.child(STORAGE_CONFIG)
        configAdapter.toSAFFile(config, safGateway, configFile)

        release()

        return Pair(ref, config)
    }

    override suspend fun abort() {
        Timber.tag(TAG).v("abort()")
        val snapshot = editorDataPub.value()

        if (snapshot.refPath != null && snapshot.pathIsNewlyPersisted) {
            if (safGateway.releasePermission(snapshot.refPath)) {
                Timber.d("Released SAF permission because it was only acquired for this editor: %s", snapshot.refPath)
            }
        }

        release()
    }

    private suspend fun release() {
        Timber.tag(TAG).v("release()")
//        editorDataPub.close()
    }

    data class Data(
        override val storageId: Storage.Id,
        override val label: String = "",
        override val existingStorage: Boolean = false,
        override val refPath: SAFPath? = null,
        val pathIsNewlyPersisted: Boolean = false
    ) : StorageEditor.Data

    @AssistedFactory
    interface Factory : StorageEditor.Factory<SAFStorageEditor>

    companion object {
        const val STORAGE_CONFIG = "storage.data"
        val TAG = logTag("Storage", "SAF", "Editor")
    }
}