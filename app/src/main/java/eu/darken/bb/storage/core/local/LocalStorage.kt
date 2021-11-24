package eu.darken.bb.storage.core.local

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.Logging.Priority.INFO
import eu.darken.bb.common.debug.logging.Logging.Priority.WARN
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.childCast
import eu.darken.bb.common.files.core.exists
import eu.darken.bb.common.files.core.local.LocalGateway
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.files.core.local.LocalPathLookup
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.common.CommonStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class LocalStorage @AssistedInject constructor(
    @Assisted storageRef: Storage.Ref,
    @Assisted storageConfig: Storage.Config,
    @ApplicationContext override val context: Context,
    moshi: Moshi,
    mmDataRepo: MMDataRepo,
    gateway: LocalGateway,
    @AppScope appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
) : CommonStorage<LocalPath, LocalPathLookup, LocalGateway>(
    moshi = moshi,
    mmDataRepo = mmDataRepo,
    gateway = gateway,
    appScope = appScope,
    dispatcherProvider = dispatcherProvider,
    tag = TAG,
), HasContext, Progress.Client {

    override val storageRef: LocalStorageRef = storageRef as LocalStorageRef
    override val storageConfig: LocalStorageConfig = storageConfig as LocalStorageConfig

    override val dataDir: LocalPath = this.storageRef.path.childCast("data")

    init {
        log(TAG, INFO) { "init(storage=$this)" }
        appScope.launch {
            if (!dataDir.exists(gateway)) {
                log(TAG, WARN) { "Data dir doesn't exist: $dataDir" }
            }
        }
    }

    @AssistedFactory
    interface Factory : Storage.Factory<LocalStorage>

    companion object {
        internal val TAG = logTag("Storage", "Local")
    }
}