package eu.darken.bb.backup.core.app.restore

import android.content.Context
import android.content.pm.ApplicationInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap
import eu.darken.bb.backup.core.app.AppBackupWrap.DataType
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.SharedResource
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.error.hasCause
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.flow.launchForAction
import eu.darken.bb.common.pkgs.AppPkg
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.pkgs.pkgops.installer.APKInstaller
import eu.darken.bb.common.progress.*
import eu.darken.bb.common.root.javaroot.JavaRootClient
import eu.darken.bb.common.root.javaroot.RootUnavailableException
import eu.darken.bb.processor.core.ProcessorScope
import eu.darken.bb.task.core.results.LogEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject

class AppRestoreEndpoint @Inject constructor(
    @ApplicationContext override val context: Context,
    private val apkInstaller: APKInstaller,
    private val javaRootClient: JavaRootClient,
    private val pkgOps: PkgOps,
    @ProcessorScope private val processorScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    restoreHandlers: @JvmSuppressWildcards Set<RestoreHandler>
) : Restore.Endpoint, Progress.Client, HasContext {

    private val progressPub = DynamicStateFlow(TAG, processorScope) { Progress.Data() }
    override val progress: Flow<Progress.Data> = progressPub.flow
    override fun updateProgress(update: suspend Progress.Data.() -> Progress.Data) = progressPub.updateAsync(onUpdate = update)

    override val sharedResource = SharedResource.createKeepAlive(TAG, processorScope + dispatcherProvider.IO)

    private val restoreHandlers = restoreHandlers.sortedBy { it.priority }

    override suspend fun restore(config: Restore.Config, backup: Backup.Unit, logListener: ((LogEvent) -> Unit)?) {
        updateProgressPrimary(R.string.progress_restoring_backup)
        updateProgressSecondary { backup.spec.getLabel(it) }
        updateProgressCount(Progress.Count.Indeterminate())

        Timber.tag(TAG).i("Restoring %s with config %s.", backup.spec, config)

        config as AppRestoreConfig
        val spec = backup.spec as AppBackupSpec
        val wrap = AppBackupWrap(backup)

        if (config.skipExistingApps && pkgOps.queryPkg(spec.packageName) != null) {
            return
        }

        val rootAvailable = try {
            javaRootClient.keepAliveWith(this)
            true
        } catch (e: Exception) {
            if (e.hasCause(RootUnavailableException::class)) false else throw e
        }

        if (config.restoreApk) {
            val request = APKInstaller.Request(
                packageName = wrap.packageName,
                baseApk = wrap.baseApk,
                splitApks = wrap.splitApks.toList(),
                useRoot = rootAvailable
            )

            val installResult = apkInstaller.forwardProgressTo(this).launchForAction(processorScope) {
                apkInstaller.keepAliveWith(this).install(request) {
                    logListener?.invoke(it)
                }
            }

            if (!installResult.success) {
                throw installResult.error!!
            }
        }

        val pkg = pkgOps.queryPkg(wrap.packageName)
        requireNotNull(pkg) { "${wrap.packageName} isn't installed." }

        val appInfo = (pkg as? AppPkg)?.applicationInfo
        requireNotNull(appInfo) { "${pkg.packageType} is currently not supported." }

        pkgOps.forceStop(appInfo.packageName)

        if (config.restoreData) {
            listOf(
                DataType.DATA_PRIVATE_PRIMARY,
                DataType.DATA_PUBLIC_PRIMARY,
                DataType.DATA_PUBLIC_SECONDARY
            ).forEach { type ->
                restoreType(type, config, spec, appInfo, wrap, logListener)
            }

            listOf(DataType.DATA_SDCARD_PRIMARY, DataType.DATA_SDCARD_SECONDARY).forEach { type ->
                restoreType(type, config, spec, appInfo, wrap, logListener)
            }
        }

        if (config.restoreCache) {
            listOf(
                DataType.CACHE_PRIVATE_PRIMARY,
                DataType.CACHE_PUBLIC_PRIMARY,
                DataType.CACHE_PUBLIC_SECONDARY
            ).forEach { type ->
                restoreType(type, config, spec, appInfo, wrap, logListener)
            }

            listOf(DataType.CACHE_SDCARD_PRIMARY, DataType.CACHE_SDCARD_SECONDARY).forEach { type ->
                restoreType(type, config, spec, appInfo, wrap, logListener)
            }
        }
    }

    private suspend fun restoreType(
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

        handler.forwardProgressTo(this).launchForAction(processorScope) {
            handler.restore(type, appInfo, config, builder, logListener)
        }
    }

    override fun toString(): String = "AppEndpoint()"

    companion object {
        val TAG = logTag("Backup", "App", "Restore")
    }

}