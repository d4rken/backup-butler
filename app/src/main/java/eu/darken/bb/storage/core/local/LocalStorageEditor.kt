package eu.darken.bb.storage.core.local

import com.squareup.moshi.Moshi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.asFile
import eu.darken.bb.common.files.core.exists
import eu.darken.bb.common.files.core.local.LocalGateway
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.moshi.fromFile
import eu.darken.bb.common.moshi.toFile
import eu.darken.bb.common.permission.Permission
import eu.darken.bb.common.permission.RuntimePermissionTool
import eu.darken.bb.common.user.UserManagerBB
import eu.darken.bb.storage.core.ExistingStorageException
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageEditor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.File

class LocalStorageEditor @AssistedInject constructor(
    @Assisted initialStorageId: Storage.Id,
    moshi: Moshi,
    private val runtimePermissionTool: RuntimePermissionTool,
    private val localGateway: LocalGateway,
    private val userManager: UserManagerBB,
    @AppScope private val appScope: CoroutineScope
) : StorageEditor {

    private val editorDataPub = DynamicStateFlow(TAG, appScope) { Data(storageId = initialStorageId) }
    override val editorData = editorDataPub.flow

    private val configAdapter = moshi.adapter(LocalStorageConfig::class.java)

    suspend fun updateLabel(label: String) = editorDataPub.updateBlocking { copy(label = label) }

    suspend fun updatePath(_path: LocalPath, importExisting: Boolean): LocalPath {
        check(!editorDataPub.value().existingStorage) { "Can't change path on an existing storage." }

        check(getMissingPermissions().isEmpty()) { "Storage permission isn't granted, how did we get here?" }

        check(_path.asFile().canWrite()) { "Can't write to path." }
        check(_path.asFile().isDirectory) { "Target is not a directory!" }

        val tweakedPath = if (localGateway.isStorageRoot(_path, userManager.currentUser)) {
            _path.child("BackupButler")
        } else {
            _path
        }

        val configFile = tweakedPath.child(STORAGE_CONFIG)

        if (configFile.exists(localGateway)) {
            if (!importExisting) throw ExistingStorageException(tweakedPath)

            load(tweakedPath)
        } else {
            editorDataPub.updateBlocking {
                copy(
                    refPath = tweakedPath,
                    label = if (this.label.isEmpty()) tweakedPath.name else this.label
                )
            }
        }

        return tweakedPath
    }

    suspend fun getMissingPermissions(): Set<Permission> {
        val missingPermissions = mutableSetOf<Permission>()
        if (!runtimePermissionTool.hasStoragePermission()) {
            missingPermissions.add(runtimePermissionTool.getRequiredStoragePermission())
        }
        return missingPermissions
    }

    override fun isValid(): Flow<Boolean> = editorData.map { data ->
        data.refPath != null
            && data.label.isNotEmpty()
            && getMissingPermissions().isEmpty()
    }

    override suspend fun load(ref: Storage.Ref): LocalStorageConfig {
        val path = (ref as LocalStorageRef).path
        return load(path)
    }

    private suspend fun load(path: LocalPath): LocalStorageConfig {
        val config = configAdapter.fromFile(File(path.asFile(), STORAGE_CONFIG))

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

        val data = editorDataPub.value()
        val ref = LocalStorageRef(
            storageId = data.storageId,
            path = data.refPath!!
        )
        val config = LocalStorageConfig(
            storageId = data.storageId,
            label = data.label
        )
        release()
        configAdapter.toFile(config, File(ref.path.asFile(), STORAGE_CONFIG))

        return ref to config
    }

    override suspend fun abort() {
        Timber.tag(TAG).v("abort()")
        release()
    }

    private suspend fun release() {
        Timber.tag(TAG).v("release()")
//        editorDataPub.close()
    }

    @AssistedFactory
    interface Factory : StorageEditor.Factory<LocalStorageEditor>

    data class Data(
        override val storageId: Storage.Id,
        override val label: String = "",
        override val existingStorage: Boolean = false,
        override val refPath: LocalPath? = null
    ) : StorageEditor.Data

    companion object {
        const val STORAGE_CONFIG = "storage.data"
        val TAG = logTag("Storage", "Local", "Editor")
    }
}

