package eu.darken.bb.storage.core.saf

import android.net.Uri
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.file.SFile
import eu.darken.bb.common.file.UriFile
import eu.darken.bb.common.file.asFile
import eu.darken.bb.common.moshi.fromFile
import eu.darken.bb.common.moshi.toFile
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageEditor
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.io.File

class SAFStorageEditor @AssistedInject constructor(
        @Assisted private val storageId: Storage.Id,
        moshi: Moshi,
        private val safTool: SAFTool
) : StorageEditor {

    private val configAdapter = moshi.adapter(SAFStorageConfig::class.java)
    private val configPub = HotData(SAFStorageConfig(storageId = storageId))
    override val config = configPub.data

    internal var refPath: UriFile? = null
    override var isExistingStorage: Boolean = false

    fun updateLabel(label: String) = configPub.update { it.copy(label = label) }

    fun updatePath(uri: Uri) {
        val path = UriFile(SFile.Type.DIRECTORY, uri)
        try {
            safTool.takePermission(path)
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Error while persisting permission")
            try {
                safTool.releasePermission(path)
            } catch (e2: Throwable) {
                Timber.tag(TAG).e(e2, "Error while releasing during error...")
            }
        }
        refPath = path
        configPub.update { it }
    }

    fun isPathValid(): Boolean {
        if (refPath == null) return false
        if (!safTool.hasPermission(refPath!!)) return false
        // TODO exists?
        return true
    }

    override fun isValid(): Observable<Boolean> = config.map {
        (isPathValid() || isExistingStorage) && it.label.isNotEmpty()
    }

    override fun load(ref: Storage.Ref): Single<Opt<Storage.Config>> = Single.fromCallable {
        ref as SAFStorageRef
        val config = Opt(configAdapter.fromFile(File(ref.path.asFile(), STORAGE_CONFIG)))
        if (config.isNotNull) {
            configPub.update { config.notNullValue() }
            isExistingStorage = true
        }
        refPath = ref.path as UriFile
        return@fromCallable config
    }

    override fun save(): Single<Pair<Storage.Ref, Storage.Config>> = Single.fromCallable {
        val config = configPub.snapshot
        val ref = SAFStorageRef(
                storageId = config.storageId,
                path = refPath!!
        )
        configAdapter.toFile(config, File(ref.path.asFile(), STORAGE_CONFIG))
        // TODO Write config via SAF
        // TODO error if existing storage?
        return@fromCallable Pair(ref, config)
    }

    @AssistedInject.Factory
    interface Factory : StorageEditor.Factory<SAFStorageEditor>

    companion object {
        const val STORAGE_CONFIG = "storage.data"
        val TAG = App.logTag("Storage", "SAF", "Editor")
    }
}