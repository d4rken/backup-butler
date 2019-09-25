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
import eu.darken.bb.common.file.SAFPath
import eu.darken.bb.common.moshi.fromSAFFile
import eu.darken.bb.common.moshi.toSAFFile
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
    private val configPub = HotData(SAFStorageConfig(storageId = storageId))
    override val config = configPub.data

    internal var refPath: SAFPath? = null
    override var isExistingStorage: Boolean = false

    fun updateLabel(label: String) = configPub.update { it.copy(label = label) }

    fun updatePath(uri: Uri) {
        val path = SAFPath.build(uri)
        try {
            safGateway.takePermission(path)
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Error while persisting permission")
            try {
                safGateway.releasePermission(path)
            } catch (e2: Throwable) {
                Timber.tag(TAG).e(e2, "Error while releasing during error...")
            }
        }
        refPath = path
        configPub.update { it }
    }

    fun isPathValid(): Boolean {
        if (refPath == null) return false
        if (!safGateway.hasPermission(refPath!!)) return false
        // TODO exists?
        return true
    }

    override fun isValid(): Observable<Boolean> = config.map {
        (isPathValid() || isExistingStorage) && it.label.isNotEmpty()
    }

    override fun load(ref: Storage.Ref): Single<Opt<Storage.Config>> = Single.fromCallable {
        ref as SAFStorageRef
        ref.path as SAFPath

        val config = configAdapter.fromSAFFile(safGateway, ref.path.child(STORAGE_CONFIG))

        if (config != null) {
            configPub.update { config }
            isExistingStorage = true
        }

        refPath = ref.path
        return@fromCallable Opt(config)
    }

    override fun save(): Single<Pair<Storage.Ref, Storage.Config>> = Single.fromCallable {
        val config = configPub.snapshot
        val ref = SAFStorageRef(
                storageId = config.storageId,
                path = refPath!!
        )

        val configFile = refPath!!.child(STORAGE_CONFIG)
        configAdapter.toSAFFile(config, safGateway, configFile)

        return@fromCallable Pair(ref, config)
    }

    @AssistedInject.Factory
    interface Factory : StorageEditor.Factory<SAFStorageEditor>

    companion object {
        const val STORAGE_CONFIG = "storage.data"
        val TAG = App.logTag("Storage", "SAF", "Editor")
    }
}