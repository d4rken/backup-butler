package eu.darken.bb.storage.core.saf

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.Logging.Priority.INFO
import eu.darken.bb.common.debug.logging.Logging.Priority.WARN
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.childCast
import eu.darken.bb.common.files.core.exists
import eu.darken.bb.common.files.core.saf.SAFGateway
import eu.darken.bb.common.files.core.saf.SAFPath
import eu.darken.bb.common.files.core.saf.SAFPathLookup
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.common.CommonStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class SAFStorage @AssistedInject constructor(
    @Assisted storageRef: Storage.Ref,
    @Assisted storageConfig: Storage.Config,
    @ApplicationContext override val context: Context,
    moshi: Moshi,
    gateway: SAFGateway,
    mmDataRepo: MMDataRepo,
    @AppScope appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
) : CommonStorage<SAFPath, SAFPathLookup, SAFGateway>(
    moshi = moshi,
    mmDataRepo = mmDataRepo,
    gateway = gateway,
    appScope = appScope,
    dispatcherProvider = dispatcherProvider,
    tag = "${TAG}:${storageRef.storageId.idString.takeLast(4)}",
    storageRef = storageRef,
    storageConfig = storageConfig
) {

    override val dataDir: SAFPath = (this.storageRef as SAFStorageRef).path.childCast("data")

    init {
        log(TAG, INFO) { "init(storage=$this)" }
        appScope.launch {
            if (!dataDir.exists(gateway)) {
                log(TAG, WARN) { "Data dir doesn't exist: $dataDir" }
            }
        }
    }

    @AssistedFactory
    interface Factory : Storage.Factory<SAFStorage>

    companion object {
        val TAG = logTag("Storage", "SAF")

    }
}