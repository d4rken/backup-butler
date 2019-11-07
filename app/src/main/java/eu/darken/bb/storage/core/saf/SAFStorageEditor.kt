package eu.darken.bb.storage.core.saf

import android.net.Uri
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.common.HotData
import eu.darken.bb.common.file.SAFGateway
import eu.darken.bb.common.file.SAFPath
import eu.darken.bb.common.moshi.fromSAFFile
import eu.darken.bb.common.moshi.toSAFFile
import eu.darken.bb.storage.core.ExistingStorageException
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageEditor
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber

class SAFStorageEditor @AssistedInject constructor(
        @Assisted initialStorageId: Storage.Id,
        moshi: Moshi,
        private val safGateway: SAFGateway
) : StorageEditor {

    private val editorDataPub = HotData(Data(storageId = initialStorageId))
    override val editorData = editorDataPub.data

    private val configAdapter = moshi.adapter(SAFStorageConfig::class.java)

    private var originalRefPath: SAFPath? = null

    fun updateLabel(label: String) = editorDataPub.update { it.copy(label = label) }

    fun updatePath(uri: Uri, importExisting: Boolean): Single<SAFPath> =
            Single.just(SAFPath.build(uri)).flatMap { updatePath(it, importExisting) }

    fun updatePath(_path: SAFPath, importExisting: Boolean): Single<SAFPath> = Single.fromCallable {
        check(!editorDataPub.snapshot.existingStorage) { "Can't change path on an existing storage." }

        check(_path.canWrite(safGateway)) { "Got permissions, but can't write to the path." }
        check(_path.isDirectory(safGateway)) { "Target is not a directory!" }

        val tweakedPath: SAFPath = if (safGateway.isStorageRoot(_path)) {
            _path.child("BackupButler")
        } else {
            _path
        }

        if (tweakedPath.child(STORAGE_CONFIG).exists(safGateway)) {
            if (!importExisting) throw ExistingStorageException(tweakedPath)

            load(tweakedPath).blockingGet()
        } else {
            editorDataPub.update {
                it.copy(refPath = tweakedPath)
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

    override fun save(): Single<Pair<Storage.Ref, Storage.Config>> = Single.fromCallable {
        val data = editorDataPub.snapshot
        data.refPath as SAFPath

        if (data.refPath != originalRefPath) {
            originalRefPath?.let { safGateway.releasePermission(it) }
        }

        require(safGateway.takePermission(data.refPath)) { "We persisted the permission but it's still unavailable?!" }

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

        return@fromCallable Pair(ref, config)
    }

    override fun release(): Completable = Completable.fromCallable {
        Timber.tag(TAG).v("release()")
    }

    data class Data(
            override val storageId: Storage.Id,
            override val label: String = "",
            override val existingStorage: Boolean = false,
            override val refPath: SAFPath? = null
    ) : StorageEditor.Data

    @AssistedInject.Factory
    interface Factory : StorageEditor.Factory<SAFStorageEditor>

    companion object {
        const val STORAGE_CONFIG = "storage.data"
        val TAG = App.logTag("Storage", "SAF", "Editor")
    }
}