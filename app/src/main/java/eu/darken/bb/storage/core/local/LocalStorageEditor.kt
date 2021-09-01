package eu.darken.bb.storage.core.local

import android.Manifest
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.common.HotData
import eu.darken.bb.common.RuntimePermissionTool
import eu.darken.bb.common.files.core.asFile
import eu.darken.bb.common.files.core.local.LocalGateway
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.moshi.fromFile
import eu.darken.bb.common.moshi.toFile
import eu.darken.bb.common.user.UserManagerBB
import eu.darken.bb.storage.core.ExistingStorageException
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageEditor
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber
import java.io.File

class LocalStorageEditor @AssistedInject constructor(
    @Assisted initialStorageId: Storage.Id,
    moshi: Moshi,
    private val runtimePermissionTool: RuntimePermissionTool,
    private val localGateway: LocalGateway,
    private val userManager: UserManagerBB
) : StorageEditor {

    private val editorDataPub = HotData(Data(storageId = initialStorageId))
    override val editorData = editorDataPub.data

    private val configAdapter = moshi.adapter(LocalStorageConfig::class.java)

    fun updateLabel(label: String) = editorDataPub.update { it.copy(label = label) }

    fun updatePath(_path: LocalPath, importExisting: Boolean): Single<LocalPath> = Single
        .fromCallable {
            check(!editorDataPub.snapshot.existingStorage) { "Can't change path on an existing storage." }

            check(isPermissionGranted()) { "Storage permission isn't granted, how did we get here?" }

            check(_path.asFile().canWrite()) { "Can't write to path." }
            check(_path.asFile().isDirectory) { "Target is not a directory!" }

            return@fromCallable if (localGateway.isStorageRoot(_path, userManager.currentUser)) {
                _path.child("BackupButler")
            } else {
                _path
            }
        }
        .flatMap { tweakedPath ->
            val configFile = tweakedPath.child(STORAGE_CONFIG)
            val nextAction = if (configFile.exists(localGateway)) {
                if (!importExisting) throw ExistingStorageException(tweakedPath)

                load(tweakedPath)
            } else {
                editorDataPub.updateRx {
                    it.copy(
                        refPath = tweakedPath,
                        label = if (it.label.isEmpty()) tweakedPath.name else it.label
                    )
                }
            }
            nextAction.ignoreElement().toSingleDefault(tweakedPath)
        }

    fun isPermissionGranted(): Boolean {
        return runtimePermissionTool.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun isValid(): Observable<Boolean> = editorData.map { data ->
        data.refPath != null
                && data.label.isNotEmpty()
                && isPermissionGranted()
    }

    override fun load(ref: Storage.Ref): Single<LocalStorageConfig> = Single.just(ref)
        .map { (it as LocalStorageRef).path }
        .flatMap { load(it) }

    private fun load(path: LocalPath): Single<LocalStorageConfig> = Single.just(path)
        .map { configAdapter.fromFile(File(path.asFile(), STORAGE_CONFIG)) }
        .doOnSuccess { config ->

            editorDataPub.update {
                it.copy(
                    refPath = path,
                    existingStorage = true,
                    storageId = config.storageId,
                    label = config.label
                )
            }
        }

    override fun save(): Single<Pair<Storage.Ref, Storage.Config>> = Single
        .fromCallable {
            val data = editorDataPub.snapshot
            val ref = LocalStorageRef(
                storageId = data.storageId,
                path = data.refPath!!
            )
            val config = LocalStorageConfig(
                storageId = data.storageId,
                label = data.label
            )

            configAdapter.toFile(config, File(ref.path.asFile(), STORAGE_CONFIG))

            return@fromCallable Pair(ref, config)
        }
        .doOnSubscribe { Timber.tag(TAG).v("save()") }
        .flatMap { release().andThen(Single.just(it)) }

    override fun abort(): Completable = Completable
        .fromCallable { editorDataPub.close() }
        .doOnSubscribe { Timber.tag(TAG).v("abort()") }

    private fun release(): Completable = Completable
        .fromCallable { editorDataPub.close() }
        .doOnSubscribe { Timber.tag(TAG).v("release()") }

    @AssistedInject.Factory
    interface Factory : StorageEditor.Factory<LocalStorageEditor>

    data class Data(
        override val storageId: Storage.Id,
        override val label: String = "",
        override val existingStorage: Boolean = false,
        override val refPath: LocalPath? = null
    ) : StorageEditor.Data

    companion object {
        const val STORAGE_CONFIG = "storage.data"
        val TAG = App.logTag("Storage", "Local", "Editor")
    }
}