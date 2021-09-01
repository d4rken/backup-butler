package eu.darken.bb.common.debug.modules

import android.util.Log
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.BackupButler
import eu.darken.bb.common.debug.*
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class AppInfoModule @AssistedInject constructor(
    @Assisted val host: DebugModuleHost,
    private val backupButler: BackupButler,
    private val installId: InstallId
) : DebugModule {
    private var previousOptions: DebugOptions = DebugOptions.default()

    init {
        host.observeOptions()
            .observeOn(Schedulers.io())
            .filter { !previousOptions.compareIgnorePath(it) && it.level <= Log.INFO }
            .doOnNext { previousOptions = it }
            .subscribe {
                Timber.tag(TAG).i("Install ID: %s", installId.installId)
                Timber.tag(TAG).i("App Info: %s", backupButler.appInfo)
                Timber.tag(TAG).d("APK Checksum MD5: %s", backupButler.checksumApkMd5)
                Timber.tag(TAG).d("APK Signatures: %s", backupButler.signatures.map { it.hashCode() })
                Timber.tag(TAG).i("Update history: %s", backupButler.updateHistory)
            }
    }

    @AssistedFactory
    interface Factory : DebugModule.Factory<AppInfoModule>

    companion object {
        internal val TAG = App.logTag("Debug", "AppInfo")
    }
}