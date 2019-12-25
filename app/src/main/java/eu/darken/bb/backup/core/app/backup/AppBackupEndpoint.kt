package eu.darken.bb.backup.core.app.backup

import android.content.Context
import android.content.pm.ApplicationInfo
import eu.darken.bb.App
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
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.files.core.local.LocalGateway
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.progress.*
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.file.FileRefSource
import eu.darken.bb.task.core.results.LogEvent
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject


class AppBackupEndpoint @Inject constructor(
        @AppContext override val context: Context,
        private val pkgOps: PkgOps,
        private val mmDataRepo: MMDataRepo,
        private val apkExporter: APKExporter,
        private val localGateway: LocalGateway,
        backupHandlers: @JvmSuppressWildcards Set<BackupHandler>
) : Backup.Endpoint, Progress.Client, HasContext, SharedHolder.HasKeepAlive<Any> {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override val keepAlive = SharedHolder.createKeepAlive(TAG)
    private var keepAliveToken: SharedHolder.Resource<Any>? = null

    private val backupHandlers = backupHandlers.sortedBy { it.priority }

    override fun backup(spec: BackupSpec, logListener: ((LogEvent) -> Unit)?): Backup.Unit {
        spec as AppBackupSpec
        val builder = AppBackupWrap(spec, Backup.Id())
        updateProgressPrimary(R.string.progress_creating_app_backup)
        updateProgressCount(Progress.Count.Indeterminate())

        if (keepAliveToken == null) keepAliveToken = keepAlive.get()

        if (spec.backupApk) {
            updateProgressPrimary(R.string.progress_apk_lookup)
            val apkData = apkExporter.getAPKFile(spec.packageName)
            updateProgressSecondary(apkData.mainSource.path)

            val baseApkRef: MMRef = mmDataRepo.create(MMRef.Request(
                    backupId = builder.backupId,
                    source = FileRefSource(apkData.mainSource)
            ))

            builder.baseApk = baseApkRef
            logListener?.invoke(LogEvent(LogEvent.Type.BACKUPPED, apkData.mainSource.path))

            val splitApkRefs = mutableListOf<MMRef>()
            apkData.splitSources.forEach { splitApk ->
                updateProgressSecondary(splitApk.path)
                val splitRef: MMRef = mmDataRepo.create(MMRef.Request(
                        backupId = builder.backupId,
                        source = FileRefSource(splitApk)
                ))
                splitApkRefs.add(splitRef)
                logListener?.invoke(LogEvent(LogEvent.Type.BACKUPPED, splitApk.path))
            }
            builder.splitApks = splitApkRefs
        }

        val appInfo = pkgOps.queryAppInfos(spec.packageName)
        requireNotNull(appInfo) { "Unable to lookup ${spec.packageName}" }
        localGateway.keepAliveWith(this)

        // TODO root stuff, only when enabled?


        // Private data
        if (spec.backupData) {
            backupData(DataType.DATA_PRIVATE_PRIMARY, builder.backupId, spec, appInfo, builder, logListener)

            backupData(DataType.DATA_PUBLIC_PRIMARY, builder.backupId, spec, appInfo, builder, logListener)

            backupData(DataType.DATA_PUBLIC_SECONDARY, builder.backupId, spec, appInfo, builder, logListener)

            // TODO sdcard clutter
        }

        if (spec.backupCache) {
            backupData(DataType.CACHE_PRIVATE_PRIMARY, builder.backupId, spec, appInfo, builder, logListener)

            backupData(DataType.CACHE_PUBLIC_PRIMARY, builder.backupId, spec, appInfo, builder, logListener)

            backupData(DataType.CACHE_PUBLIC_SECONDARY, builder.backupId, spec, appInfo, builder, logListener)

            // TODO public cache
        }

        return builder.createUnit()
    }

    private fun backupData(
            type: DataType,
            backupId: Backup.Id,
            spec: AppBackupSpec,
            appInfo: ApplicationInfo,
            builder: AppBackupWrap,
            logListener: ((LogEvent) -> Unit)?
    ) {
        val handler = backupHandlers.first {
            it.isResponsible(type, spec, appInfo)
        }

        Timber.tag(TAG).d("Processing type=%s for pkg=%s with handler:%s", type, appInfo.packageName, handler)

        val dataProgress = handler.forwardProgressTo(this)
        return try {
            val refs = handler.backup(type, backupId, spec, appInfo, logListener)
            builder.putDataType(type, refs)
        } finally {
            dataProgress.dispose()
        }
    }

    override fun close() {
        keepAliveToken?.close()
    }

    override fun toString(): String = "AppEndpoint()"

    companion object {
        val TAG = App.logTag("Backup", "App", "BackupEndpoint")
    }
}