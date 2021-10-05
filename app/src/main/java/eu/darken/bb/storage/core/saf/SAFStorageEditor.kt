package eu.darken.bb.storage.core.saf

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.R
import eu.darken.bb.common.ApiHelper
import eu.darken.bb.common.HotData
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.saf.SAFGateway
import eu.darken.bb.common.files.core.saf.SAFPath
import eu.darken.bb.common.moshi.fromSAFFile
import eu.darken.bb.common.moshi.toSAFFile
import eu.darken.bb.storage.core.ExistingStorageException
import eu.darken.bb.storage.core.IllegalStoragePathException
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageEditor
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber

class SAFStorageEditor @AssistedInject constructor(
    @Assisted initialStorageId: Storage.Id,
    @ApplicationContext private val context: Context,
    moshi: Moshi,
    private val safGateway: SAFGateway
) : StorageEditor {

    private val editorDataPub = HotData(tag = TAG) { Data(storageId = initialStorageId) }
    override val editorData = editorDataPub.data

    private val configAdapter = moshi.adapter(SAFStorageConfig::class.java)

    private var originalRefPath: SAFPath? = null

    fun updateLabel(label: String) = editorDataPub.update { it.copy(label = label) }

    fun updatePath(_path: SAFPath, importExisting: Boolean, isNewlyPersisted: Boolean? = null): Single<SAFPath> =
        Single.fromCallable {
            check(!editorDataPub.snapshot.existingStorage) { "Can't change path on an existing storage." }

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

                load(tweakedPath).blockingGet()
            } else {
                editorDataPub.update {
                    it.copy(
                        label = if (it.label.isEmpty()) tweakedPath.userReadableName(context) else it.label,
                        refPath = tweakedPath,
                        pathIsNewlyPersisted = isNewlyPersisted ?: it.pathIsNewlyPersisted
                    )
                }
            }

            return@fromCallable tweakedPath
        }

    override fun isValid(): Observable<Boolean> = editorData.map {
        it.refPath != null && it.label.isNotEmpty()
    }

    override fun load(ref: Storage.Ref): Single<SAFStorageConfig> = Single.just(ref)
        .map { (it as SAFStorageRef).path }
        .flatMap { load(it) }

    private fun load(path: SAFPath): Single<SAFStorageConfig> = Single.just(path)
        .map { configAdapter.fromSAFFile(safGateway, it.child(STORAGE_CONFIG)) }
        .doOnSuccess { config ->
            originalRefPath = path

            editorDataPub.update {
                it.copy(
                    refPath = path,
                    existingStorage = true,
                    storageId = config.storageId,
                    label = config.label
                )
            }
        }

    override fun save(): Single<Pair<Storage.Ref, Storage.Config>> = editorDataPub
        .updateRx { data ->
            data.refPath as SAFPath

            if (data.refPath != originalRefPath) {
                originalRefPath?.let { safGateway.releasePermission(it) }
            }

            require(safGateway.hasPermission(data.refPath)) { "No permission persisted for ${data.refPath}?!" }

            data.copy(
                pathIsNewlyPersisted = false
            )
        }
        .doOnSubscribe { Timber.tag(TAG).v("save()") }
        .map { (_, data) ->
            data.refPath as SAFPath

            val ref = SAFStorageRef(
                storageId = data.storageId,
                path = data.refPath
            )
            val config = SAFStorageConfig(
                storageId = data.storageId,
                label = data.label
            )

            val configFile = ref.path.child(STORAGE_CONFIG)
            configAdapter.toSAFFile(config, safGateway, configFile)

            return@map Pair(ref, config)
        }
        .flatMap { release().andThen(Single.just(it)) }


    override fun abort(): Completable = editorDataPub.latest
        .doOnSubscribe { Timber.tag(TAG).v("abort()") }
        .doOnSuccess { data ->
            if (data.refPath != null && data.pathIsNewlyPersisted) {
                if (safGateway.releasePermission(data.refPath)) {
                    Timber.d("Released SAF permission because it was only acquired for this editor: %s", data.refPath)
                }
            }
        }
        .flatMapCompletable { release() }

    private fun release(): Completable = Completable
        .fromCallable { editorDataPub.close() }
        .doOnSubscribe { Timber.tag(TAG).v("release()") }

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