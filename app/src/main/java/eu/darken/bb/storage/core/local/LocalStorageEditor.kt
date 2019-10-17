package eu.darken.bb.storage.core.local

import android.Manifest
import android.os.Environment
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.common.HotData
import eu.darken.bb.common.RuntimePermissionTool
import eu.darken.bb.common.file.JavaPath
import eu.darken.bb.common.file.asFile
import eu.darken.bb.common.moshi.fromFile
import eu.darken.bb.common.moshi.toFile
import eu.darken.bb.storage.core.ExistingStorageException
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageEditor
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File

class LocalStorageEditor @AssistedInject constructor(
        @Assisted private val storageId: Storage.Id,
        moshi: Moshi,
        private val runtimePermissionTool: RuntimePermissionTool
) : StorageEditor {

    private val configAdapter = moshi.adapter(LocalStorageConfig::class.java)
    private val editorDataPub = HotData(Data(storageId = storageId))
    override val editorData = editorDataPub.data

    fun updateLabel(label: String) = editorDataPub.update { it.copy(label = label) }

    fun updatePath(path: String, importExisting: Boolean): Single<JavaPath> =
            Single.just(JavaPath.build(path)).flatMap { updatePath(it, importExisting) }

    fun updatePath(path: JavaPath, importExisting: Boolean): Single<JavaPath> = Single.fromCallable {
        check(!editorDataPub.snapshot.existingStorage) { "Can't change path on an existing storage." }

        check(isPermissionGranted()) { "Storage permission isn't granted, how did we get here?" }

        check(path.asFile().canWrite()) { "Can't write to path." }
        check(path.asFile().isDirectory) { "Target is not a directory!" }

        val configFile = File(path.asFile(), STORAGE_CONFIG)
        if (configFile.exists()) {
            if (!importExisting) throw ExistingStorageException(path)

            load(path).blockingGet()
        } else {
            editorDataPub.update {
                it.copy(refPath = path)
            }
        }

        return@fromCallable path
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

    private fun load(path: JavaPath): Single<LocalStorageConfig> = Single.just(path)
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

    override fun save(): Single<Pair<Storage.Ref, Storage.Config>> = Single.fromCallable {
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

    override fun release(): Completable = Completable.complete()

    @AssistedInject.Factory
    interface Factory : StorageEditor.Factory<LocalStorageEditor>

    companion object {
        const val STORAGE_CONFIG = "storage.data"
    }

    data class Data(
            override val storageId: Storage.Id,
            override val label: String = "",
            override val existingStorage: Boolean = false,
            override val refPath: JavaPath? = JavaPath.build(File(Environment.getExternalStorageDirectory(), "BackupButler"))
    ) : StorageEditor.Data
}