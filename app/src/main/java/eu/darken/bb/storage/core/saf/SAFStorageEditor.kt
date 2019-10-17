package eu.darken.bb.storage.core.saf

import android.content.Context
import android.net.Uri
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.SAFGateway
import eu.darken.bb.common.file.SAFPath
import eu.darken.bb.common.moshi.fromSAFFile
import eu.darken.bb.common.moshi.toSAFFile
import eu.darken.bb.common.opt
import eu.darken.bb.storage.core.ExistingStorageException
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageEditor
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber

class SAFStorageEditor @AssistedInject constructor(
        @Assisted private val storageId: Storage.Id,
        @AppContext private val context: Context,
        moshi: Moshi,
        private val safGateway: SAFGateway
) : StorageEditor {

    private val configAdapter = moshi.adapter(SAFStorageConfig::class.java)
    private val editorDataPub = HotData(Data(storageId = storageId))
    override val editorData = editorDataPub.data

    fun updateLabel(label: String) = editorDataPub.update { it.copy(label = label) }

    fun updatePath(uri: Uri, importExisting: Boolean): Single<SAFPath> =
            Single.just(SAFPath.build(uri)).flatMap { updatePath(it, importExisting) }

    fun updatePath(path: SAFPath, importExisting: Boolean): Single<SAFPath> = Single.fromCallable {
        check(!editorDataPub.snapshot.existingStorage) { "Can't change path on an existing storage." }

        try {
            safGateway.takePermission(path)
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Error while persisting permission")
            try {
                safGateway.releasePermission(path)
            } catch (e2: Throwable) {
                Timber.tag(TAG).e(e2, "Error while releasing during error...")
            }
            throw e
        }

        check(safGateway.hasPermission(path)) { "We persisted the permission but it's still unavailable?!" }

        check(path.canWrite(safGateway)) { "Got permissions, but can't write to the path." }
        check(path.isDirectory(safGateway)) { "Target is not a directory!" }

        if (path.child(STORAGE_CONFIG).exists(safGateway)) {
            if (!importExisting) throw ExistingStorageException(path)

            val optConfig = load(path).blockingGet()
            requireNotNull(optConfig.value) { "Failed to load config from existing storage." }

        } else {
            editorDataPub.update {
                it.copy(refPath = path)
            }
        }

        return@fromCallable path
    }

    override fun isValid(): Observable<Boolean> = editorData.map {
        it.refPath != null && it.label.isNotEmpty()
    }

    override fun load(ref: Storage.Ref): Single<Opt<Storage.Config>> = Single.just(ref)
            .map { (it as SAFStorageRef).path }
            .flatMap { load(it) }

    private fun load(path: SAFPath): Single<Opt<Storage.Config>> = Single.just(path)
            .map { configAdapter.fromSAFFile(safGateway, it.child(STORAGE_CONFIG)).opt() }
            .doOnSuccess { optConfig ->
                if (optConfig.isNull) return@doOnSuccess
                val config = optConfig.notNullValue()
                require(config.storageType == Storage.Type.SAF) { "Can only import storage of same type." }
                editorDataPub.update {
                    it.copy(
                            refPath = path,
                            existingStorage = optConfig.isNotNull,
                            storageId = config.storageId,
                            label = config.label
                    )
                }
            }
            .map { it as Opt<Storage.Config> }

    override fun save(): Single<Pair<Storage.Ref, Storage.Config>> = Single.fromCallable {
        val data = editorDataPub.snapshot
        val ref = SAFStorageRef(
                storageId = data.storageId,
                path = data.refPath!!
        )
        val config = SAFStorageConfig(
                storageId = data.storageId,
                label = data.label
        )

        val configFile = ref.path.child(STORAGE_CONFIG)
        configAdapter.toSAFFile(config, safGateway, configFile)

        return@fromCallable Pair(ref, config)
    }

    @AssistedInject.Factory
    interface Factory : StorageEditor.Factory<SAFStorageEditor>

    companion object {
        const val STORAGE_CONFIG = "storage.data"
        val TAG = App.logTag("Storage", "SAF", "Editor")
    }

    data class Data(
            override val storageId: Storage.Id,
            override val label: String = "",
            override val existingStorage: Boolean = false,
            override val refPath: SAFPath? = null
    ) : StorageEditor.Data
}