package eu.darken.bb.backup.core.app.backup

import android.content.Context
import android.content.pm.ApplicationInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.app.APKExporter
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap
import eu.darken.bb.backup.core.app.AppBackupWrap.DataType
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.errors.hasCause
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.GatewaySwitch
import eu.darken.bb.common.getString
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.progress.*
import eu.darken.bb.common.root.javaroot.JavaRootClient
import eu.darken.bb.common.root.javaroot.RootUnavailableException
import eu.darken.bb.common.rx.withScopeThis
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.generic.GenericRefSource
import eu.darken.bb.task.core.results.LogEvent
import io.reactivex.rxjava3.core.Observable
import timber.log.Timber
import javax.inject.Inject


class AppBackupEndpoint @Inject constructor(
    @ApplicationContext override val context: Context,
    private val pkgOps: PkgOps,
    private val mmDataRepo: MMDataRepo,
    private val apkExporter: APKExporter,
    private val gatewaySwitch: GatewaySwitch,
    private val javaRootClient: JavaRootClient,
    backupHandlers: @JvmSuppressWildcards Set<BackupHandler>
) : Backup.Endpoint, Progress.Client, HasContext {

    private val progressPub = HotData(tag = TAG) { Progress.Data() }
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override val keepAlive = SharedHolder.createKeepAlive(TAG)

    private val backupHandlers = backupHandlers.sortedBy { it.priority }

    override fun backup(spec: BackupSpec, logListener: ((LogEvent) -> Unit)?): Backup.Unit {
        spec as AppBackupSpec
        val builder = AppBackupWrap(spec, Backup.Id())
        updateProgressPrimary(R.string.progress_creating_app_backup)
        updateProgressCount(Progress.Count.Indeterminate())

        gatewaySwitch.keepAliveWith(this)
        pkgOps.keepAliveWith(this)

        if (spec.backupApk) {
            updateProgressPrimary(R.string.progress_apk_lookup)
            val apkData = apkExporter.getAPKFile(spec.packageName)
            updateProgressSecondary(apkData.mainSource.path)

            val baseApkRef: MMRef = mmDataRepo.create(
                MMRef.Request(
                    backupId = builder.backupId,
                    source = GenericRefSource(
                        gateway = gatewaySwitch,
                        label = getString(DataType.APK_BASE.labelRes),
                        path = apkData.mainSource
                    )
                )
            )

            builder.baseApk = baseApkRef
            logListener?.invoke(LogEvent(LogEvent.Type.BACKUPPED, apkData.mainSource.path))

            val splitApkRefs = mutableListOf<MMRef>()
            apkData.splitSources.forEach { splitApk ->
                updateProgressSecondary(splitApk.path)
                val splitRef: MMRef = mmDataRepo.create(
                    MMRef.Request(
                        backupId = builder.backupId,
                        source = GenericRefSource(
                            gateway = gatewaySwitch,
                            label = getString(DataType.APK_SPLIT.labelRes),
                            path = splitApk
                        )
                    )
                )
                splitApkRefs.add(splitRef)
                logListener?.invoke(LogEvent(LogEvent.Type.BACKUPPED, splitApk.path))
            }
            builder.splitApks = splitApkRefs
        }

        val appInfo = pkgOps.queryAppInfos(spec.packageName)
        requireNotNull(appInfo) { "Unable to lookup ${spec.packageName}" }

        val rootAvailable = try {
            javaRootClient.keepAliveWith(this)
            true
        } catch (e: Exception) {
            if (e.hasCause(RootUnavailableException::class)) false else throw e
        }
        // TODO root stuff, only when enabled?

        if (spec.backupData) {
            listOf(
                DataType.DATA_PRIVATE_PRIMARY,
                DataType.DATA_PUBLIC_PRIMARY,
                DataType.DATA_PUBLIC_SECONDARY
            ).forEach { type ->
                backupType(type, builder.backupId, spec, appInfo, builder, logListener)
            }

            listOf(DataType.DATA_SDCARD_PRIMARY, DataType.DATA_SDCARD_SECONDARY).forEach { type ->
                backupType(type, builder.backupId, spec, appInfo, builder, logListener)
            }
        }

        if (spec.backupCache) {
            listOf(
                DataType.CACHE_PRIVATE_PRIMARY,
                DataType.CACHE_PUBLIC_PRIMARY,
                DataType.CACHE_PUBLIC_SECONDARY
            ).forEach { type ->
                backupType(type, builder.backupId, spec, appInfo, builder, logListener)
            }


            listOf(DataType.CACHE_SDCARD_PRIMARY, DataType.CACHE_SDCARD_SECONDARY).forEach { type ->
                backupType(type, builder.backupId, spec, appInfo, builder, logListener)
            }
        }

        spec.extraPaths.forEach { extra ->
            backupExtra(builder.backupId, spec, appInfo, builder, extra, logListener)
        }

        return builder.createUnit()
    }

    private fun backupType(
        type: DataType,
        backupId: Backup.Id,
        spec: AppBackupSpec,
        appInfo: ApplicationInfo,
        builder: AppBackupWrap,
        logListener: ((LogEvent) -> Unit)?
    ) {
        val handler = backupHandlers.first {
            it.isResponsible(
                type = type,
                config = spec,
                appInfo = appInfo,
                target = null
            )
        }

        Timber.tag(TAG).d("Processing type=%s for pkg=%s with handler:%s", type, appInfo.packageName, handler)

        handler.keepAliveWith(this)

        handler.forwardProgressTo(this).withScopeThis {
            handler.backup(
                type = type,
                backupId = backupId,
                spec = spec,
                appInfo = appInfo,
                wrap = builder,
                target = null,
                logListener = logListener
            )
        }
    }

    private fun backupExtra(
        backupId: Backup.Id,
        spec: AppBackupSpec,
        appInfo: ApplicationInfo,
        builder: AppBackupWrap,
        target: APath,
        logListener: ((LogEvent) -> Unit)?
    ) {
        val handler = backupHandlers.first {
            it.isResponsible(
                type = DataType.EXTRA_PATHS,
                config = spec,
                appInfo = appInfo,
                target = null
            )
        }

        Timber.tag(TAG).d("Processing pkg=%s extra=%s with handler:%s", appInfo.packageName, target, handler)

        handler.keepAliveWith(this)

        handler.forwardProgressTo(this).withScopeThis {
            handler.backup(
                type = DataType.EXTRA_PATHS,
                backupId = backupId,
                spec = spec,
                appInfo = appInfo,
                wrap = builder,
                target = target,
                logListener = logListener
            )
        }
    }

    override fun toString(): String = "AppEndpoint()"

    companion object {
        val TAG = logTag("Backup", "App", "BackupEndpoint")
    }
}