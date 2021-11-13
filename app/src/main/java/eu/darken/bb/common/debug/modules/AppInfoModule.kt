package eu.darken.bb.common.debug.modules

import android.util.Log
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.BackupButler
import eu.darken.bb.common.debug.*
import eu.darken.bb.common.debug.logging.logTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class AppInfoModule @AssistedInject constructor(
    @Assisted val host: DebugModuleHost,
    private val backupButler: BackupButler,
    private val installId: InstallId,
    @DebugScope private val debugScope: CoroutineScope,
) : DebugModule {
    private var previousOptions: DebugOptions = DebugOptions.default()

    init {
        host.observeOptions()
            .filter { !previousOptions.compareIgnorePath(it) && it.level <= Log.INFO }
            .onEach { previousOptions = it }
            .onEach {
                Timber.tag(TAG).i("Install ID: %s", installId.installId)
                Timber.tag(TAG).i("App Info: %s", backupButler.appInfo)
                Timber.tag(TAG).d("APK Checksum MD5: %s", backupButler.checksumApkMd5)
                Timber.tag(TAG).d("APK Signatures: %s", backupButler.signatures.map { it.hashCode() })
                Timber.tag(TAG).i("Update history: %s", backupButler.updateHistory)
            }
            .launchIn(debugScope)
    }

    @AssistedFactory
    interface Factory : DebugModule.Factory<AppInfoModule>

    companion object {
        internal val TAG = logTag("Debug", "AppInfo")
    }
}