package eu.darken.bb.backup.core.app.restore

import android.content.Context
import android.content.pm.ApplicationInfo
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap
import eu.darken.bb.backup.core.app.AppBackupWrap.Type
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.files.core.GatewaySwitch
import eu.darken.bb.common.hasCause
import eu.darken.bb.common.pkgs.AppPkg
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.pkgs.pkgops.installer.APKInstaller
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import eu.darken.bb.common.root.core.javaroot.RootUnavailableException
import io.reactivex.Observable
import javax.inject.Inject

class AppRestoreEndpoint @Inject constructor(
        @AppContext override val context: Context,
        private val javaRootClient: JavaRootClient,
        private val apkInstaller: APKInstaller,
        private val gatewaySwitch: GatewaySwitch,
        private val pkgOps: PkgOps,
        restoreHandlers: @JvmSuppressWildcards Set<RestoreHandler>
) : Restore.Endpoint, Progress.Client, HasContext {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    private var rootToken: SharedHolder.Resource<JavaRootClient.Connection>? = null
    private var rootAvailable = true

    private val restoreHandlers = restoreHandlers.sortedBy { it.priority }

    override fun restore(config: Restore.Config, backup: Backup.Unit): Boolean {
        updateProgressPrimary(R.string.progress_restoring_backup)
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        config as AppRestoreConfig
        val spec = backup.spec as AppBackupSpec
        val wrap = AppBackupWrap(backup)

        updateProgressCount(Progress.Count.Counter(0, wrap.data.size))

        if (config.skipExistingApps && pkgOps.queryPkg(spec.packageName) != null) {
            // TODO skip if pkg already exists?, result?
        }

        if (rootAvailable && rootToken == null) {
            try {
                rootToken = javaRootClient.client.get()
            } catch (e: Exception) {
                if (e.hasCause(RootUnavailableException::class)) rootAvailable = false
                else throw e
            }
        }

        val request = APKInstaller.Request(
                packageName = wrap.packageName,
                baseApk = wrap.baseApk,
                splitApks = wrap.splitApks.toList(),
                useRoot = rootAvailable
        )
        // TODO check result, error?
        val installResult = apkInstaller.install(request)
        // TODO if we don't restore the APK and it's not installed then we can't restore data, error? log? result?

        val pkg = pkgOps.queryPkg(wrap.packageName)
        requireNotNull(pkg) { "${wrap.packageName} isn't installed." }

        val appInfo = (pkg as? AppPkg)?.applicationInfo
        requireNotNull(appInfo) { "${pkg.packageType} is currently not supported." }

        // TODO if the app is running force stop it

        if (config.restoreData) {
            val results = restoreData(config, spec, appInfo, wrap)
        }

        if (config.restoreCache) {
            val results = restoreCache(config, spec, appInfo, wrap)
        }

        // TODO return result?
        return true
    }

    private fun restoreData(
            config: AppRestoreConfig,
            spec: AppBackupSpec,
            appInfo: ApplicationInfo,
            wrap: AppBackupWrap
    ): Collection<RestoreHandler.Result> {
        val results = mutableListOf<RestoreHandler.Result>()

        // TODO Copy private data
        val handler = restoreHandlers.first {
            it.isResponsible(Type.DATA_PRIVATE_PRIMARY, config, spec)
        }
        val privateDataRestore = handler.restore(appInfo, config, Type.DATA_PRIVATE_PRIMARY, wrap)
        results.add(privateDataRestore)


        // TODO Copy public data

        // TODO Copy public clutter

        return results
    }

    private fun restoreCache(
            config: AppRestoreConfig,
            spec: AppBackupSpec,
            appInfo: ApplicationInfo,
            wrap: AppBackupWrap
    ): Collection<RestoreHandler.Result> {
        val results = mutableListOf<RestoreHandler.Result>()

        // TODO Copy private cache
        // TODO Copy private data
        val handler = restoreHandlers.first {
            it.isResponsible(Type.CACHE_PRIVATE_PRIMARY, config, spec)
        }
        val privateCacheRestore = handler.restore(appInfo, config, Type.CACHE_PRIVATE_PRIMARY, wrap)
        results.add(privateCacheRestore)

        // TODO Copy public cache

        return results
    }

    override fun close() {
        rootToken?.close()
//        TODO("not implemented")
    }

    override fun toString(): String = "AppEndpoint()"

    companion object {
        val TAG = App.logTag("Backup", "App", "Restore")
    }

}