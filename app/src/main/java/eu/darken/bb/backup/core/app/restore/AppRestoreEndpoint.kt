package eu.darken.bb.backup.core.app.restore

import android.content.Context
import android.content.pm.ApplicationInfo
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap
import eu.darken.bb.backup.core.app.AppBackupWrap.DataType
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.hasCause
import eu.darken.bb.common.pkgs.AppPkg
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.pkgs.pkgops.installer.APKInstaller
import eu.darken.bb.common.progress.*
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import eu.darken.bb.common.root.core.javaroot.RootUnavailableException
import eu.darken.bb.common.rx.withScopeThis
import eu.darken.bb.task.core.results.LogEvent
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class AppRestoreEndpoint @Inject constructor(
        @AppContext override val context: Context,
        private val apkInstaller: APKInstaller,
        private val javaRootClient: JavaRootClient,
        private val pkgOps: PkgOps,
        restoreHandlers: @JvmSuppressWildcards Set<RestoreHandler>
) : Restore.Endpoint, Progress.Client, HasContext {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override val keepAlive = SharedHolder.createKeepAlive(TAG)

    private val restoreHandlers = restoreHandlers.sortedBy { it.priority }

    override fun restore(config: Restore.Config, backup: Backup.Unit, logListener: ((LogEvent) -> Unit)?): Boolean {
        updateProgressPrimary(R.string.progress_restoring_backup)
        updateProgressSecondary { backup.spec.getLabel(it) }
        updateProgressCount(Progress.Count.Indeterminate())

        Timber.tag(TAG).i("Restoring %s with config %s.", backup.spec, config)

        config as AppRestoreConfig
        val spec = backup.spec as AppBackupSpec
        val wrap = AppBackupWrap(backup)

        if (config.skipExistingApps && pkgOps.queryPkg(spec.packageName) != null) {
            // TODO skip if pkg already exists?, result?
        }

        val rootAvailable = try {
            javaRootClient.keepAliveWith(this)
            true
        } catch (e: Exception) {
            if (e.hasCause(RootUnavailableException::class)) false
            else throw e
        }

        val request = APKInstaller.Request(
                packageName = wrap.packageName,
                baseApk = wrap.baseApk,
                splitApks = wrap.splitApks.toList(),
                useRoot = rootAvailable
        )
        // TODO check result, error?
        val installResult = apkInstaller.forwardProgressTo(this).withScopeThis {
            apkInstaller.keepAliveWIth(this).install(request) {
                logListener?.invoke(it)
            }
        }

        // TODO if we don't restore the APK and it's not installed then we can't restore data, error? log? result?


        val pkg = pkgOps.queryPkg(wrap.packageName)
        requireNotNull(pkg) { "${wrap.packageName} isn't installed." }

        val appInfo = (pkg as? AppPkg)?.applicationInfo
        requireNotNull(appInfo) { "${pkg.packageType} is currently not supported." }

        pkgOps.forceStop(appInfo.packageName)

        if (config.restoreData) {
            listOf(DataType.DATA_PRIVATE_PRIMARY, DataType.DATA_PUBLIC_PRIMARY, DataType.DATA_PUBLIC_SECONDARY).forEach { type ->
                backupType(type, config, spec, appInfo, wrap, logListener)
            }

            // TODO sdcard clutter
        }

        if (config.restoreCache) {
            listOf(DataType.CACHE_PRIVATE_PRIMARY, DataType.CACHE_PUBLIC_PRIMARY, DataType.CACHE_PUBLIC_SECONDARY).forEach { type ->
                backupType(type, config, spec, appInfo, wrap, logListener)
            }
        }

        // TODO return result?
        return true
    }

    private fun backupType(
            type: DataType,
            config: AppRestoreConfig,
            spec: AppBackupSpec,
            appInfo: ApplicationInfo,
            builder: AppBackupWrap,
            logListener: ((LogEvent) -> Unit)?
    ) {
        val handler = restoreHandlers.first {
            it.isResponsible(type, config, spec)
        }

        Timber.tag(TAG).d("Processing type=%s for pkg=%s with handler:%s", type, appInfo.packageName, handler)

        handler.keepAliveWith(this)

        handler.forwardProgressTo(this).withScopeThis {
            handler.restore(type, appInfo, config, builder, logListener)
        }
    }

    override fun toString(): String = "AppEndpoint()"

    companion object {
        val TAG = App.logTag("Backup", "App", "Restore")
    }

}