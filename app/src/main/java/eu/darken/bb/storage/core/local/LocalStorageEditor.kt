package eu.darken.bb.storage.core.local

import android.Manifest
import android.os.Environment
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.RuntimePermissionTool
import eu.darken.bb.common.file.JavaPath
import eu.darken.bb.common.file.asFile
import eu.darken.bb.common.moshi.fromFile
import eu.darken.bb.common.moshi.toFile
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageEditor
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File

class LocalStorageEditor @AssistedInject constructor(
        @Assisted private val storageId: Storage.Id,
        moshi: Moshi,
        private val runtimePermissionTool: RuntimePermissionTool
) : StorageEditor {

    private val configAdapter = moshi.adapter(LocalStorageConfig::class.java)
    private val configPub = HotData(LocalStorageConfig(storageId = storageId))
    override val config = configPub.data

    internal var refPath = File(Environment.getExternalStorageDirectory(), "BackupButler")

    override var isExistingStorage: Boolean = false

    var rawPath: String = refPath.path

    fun updateLabel(label: String) = configPub.update { it.copy(label = label) }

    fun updatePath(newPath: String) {
        if (isExistingStorage && newPath != rawPath) {
            throw IllegalArgumentException("Can't update path on existing storage")
        }
        rawPath = newPath
        if (isRawPathValid()) {
            refPath = File(newPath)
        }
        configPub.update { it }
    }

    fun isRawPathValid(): Boolean {
        var file = File(rawPath)
        if (file.exists()) return false
        return try {
            while (!file.exists() && file.parent != null) {
                file = file.parentFile
            }
            file.isDirectory && file.canRead() && file.canWrite() && file.canExecute()
        } catch (e: Exception) {
            false
        }
    }

    fun isPermissionGranted(): Boolean {
        return runtimePermissionTool.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun isValid(): Observable<Boolean> = config.map {
        (isRawPathValid() || isExistingStorage)
                && it.label.isNotEmpty()
                && isPermissionGranted()
    }

    override fun load(ref: Storage.Ref): Single<Opt<Storage.Config>> = Single.fromCallable {
        ref as LocalStorageRef
        val config = Opt(configAdapter.fromFile(File(ref.path.asFile(), STORAGE_CONFIG)))
        if (config.isNotNull) {
            configPub.update { config.notNullValue() }
            isExistingStorage = true
        }
        refPath = ref.path.asFile()
        return@fromCallable config
    }

    override fun save(): Single<Pair<Storage.Ref, Storage.Config>> = Single.fromCallable {
        val config = configPub.snapshot
        val ref = LocalStorageRef(
                storageId = config.storageId,
                path = JavaPath.build(refPath)
        )
        configAdapter.toFile(config, File(ref.path.asFile(), STORAGE_CONFIG))
        return@fromCallable Pair(ref, config)
    }

    @AssistedInject.Factory
    interface Factory : StorageEditor.Factory<LocalStorageEditor>

    companion object {
        const val STORAGE_CONFIG = "storage.data"
    }
}