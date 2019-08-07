package eu.darken.bb.storage.core.local

import android.os.Environment
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.file.JavaFile
import eu.darken.bb.common.file.SFile
import eu.darken.bb.common.file.asFile
import eu.darken.bb.common.file.asSFile
import eu.darken.bb.common.moshi.fromFile
import eu.darken.bb.common.moshi.toFile
import eu.darken.bb.storage.core.StorageConfig
import eu.darken.bb.storage.core.StorageEditor
import eu.darken.bb.storage.core.StorageRef
import io.reactivex.Single
import java.io.File
import java.util.*

class LocalStorageEditor @AssistedInject constructor(
        moshi: Moshi,
        @Assisted private val storageId: UUID
) : StorageEditor {

    private val configAdapter = moshi.adapter(LocalStorageConfig::class.java)
    private val configPub = HotData(LocalStorageConfig(
            label = "",
            storageId = storageId
    ))
    val config = configPub.data
    private var existingConfig: Boolean = false
    internal var refPath: SFile? = File(Environment.getExternalStorageDirectory(), "BackupButler").asSFile()

    fun updateLabel(label: String) = configPub.update { it.copy(label = label) }

    fun updateRefPath(path: String) {
        if (!isRefPathValid(path)) return
        refPath = JavaFile.build(path)
    }

    internal fun isRefPathValid(path: String?): Boolean {
        if (path == null) return false
        var file = File(path)
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

    override fun isExistingStorage(): Boolean = existingConfig

    override fun isValidConfig(): Boolean {
        return refPath != null
                && configPub.snapshot.label.isNotEmpty()
    }

    override fun load(ref: StorageRef): Single<Opt<StorageConfig>> = Single.fromCallable {
        ref as LocalStorageRef
        val config = Opt(configAdapter.fromFile(File(ref.path.asFile(), STORAGE_CONFIG)))
        if (config.isNotNull) {
            configPub.update { config.notNullValue() }
            existingConfig = true
        }
        refPath = ref.path
        return@fromCallable config
    }

    override fun save(): Single<Pair<StorageRef, StorageConfig>> = Single.fromCallable {
        val config = configPub.snapshot
        val ref = LocalStorageRef(
                storageId = config.storageId,
                path = refPath ?: throw IllegalStateException("Refpath not set")
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