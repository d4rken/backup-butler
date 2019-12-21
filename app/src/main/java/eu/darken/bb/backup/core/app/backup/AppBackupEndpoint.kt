package eu.darken.bb.backup.core.app.backup

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.app.APKExporter
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.files.core.local.LocalGateway
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.files.core.walk
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.archive.APathArchiveSource
import eu.darken.bb.processor.core.mm.file.FileRefSource
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject


class AppBackupEndpoint @Inject constructor(
        @AppContext override val context: Context,
        private val pkgOps: PkgOps,
        private val mmDataRepo: MMDataRepo,
        private val apkExporter: APKExporter,
        private val localGateway: LocalGateway
) : Backup.Endpoint, Progress.Client, HasContext, SharedHolder.HasKeepAlive<Any> {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override val keepAlive = SharedHolder.createKeepAlive(TAG)
    private var keepAliveToken: SharedHolder.Resource<Any>? = null

    override fun backup(spec: BackupSpec): Backup.Unit {
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

            val splitApkRefs = mutableListOf<MMRef>()
            apkData.splitSources.forEach { splitApk ->
                updateProgressSecondary(splitApk.path)
                val splitRef: MMRef = mmDataRepo.create(MMRef.Request(
                        backupId = builder.backupId,
                        source = FileRefSource(splitApk)
                ))
                splitApkRefs.add(splitRef)
            }
            builder.splitApks = splitApkRefs
        }

        val appInfo = pkgOps.queryAppInfos(spec.packageName)
        requireNotNull(appInfo) { "Unable to lookup ${spec.packageName}" }
        localGateway.keepAliveWith(this)

        // TODO root stuff, only when enabled?
        val walkedPath = LocalPath.build(appInfo.dataDir)
        val items = localGateway.keepAlive.get().use {
            walkedPath.walk(localGateway)
                    .filterNot { it == walkedPath }
                    .onEach { Timber.tag(TAG).v("To backup: %s", it) }
                    .map { it.lookup(localGateway) }
                    .toList()
        }

        // Private data
        if (spec.backupData) {
            val nonCacheItems = items.filterNot {
                it.path.startsWith(LocalPath.build(appInfo.dataDir, "cache").path)
            }

            val dataRef: MMRef = mmDataRepo.create(MMRef.Request(
                    backupId = builder.backupId,
                    source = APathArchiveSource(localGateway, LocalPath.build(appInfo.dataDir), nonCacheItems)
            ))
            builder.putType(AppBackupWrap.Type.DATA_PRIVATE_PRIMARY, listOf(dataRef))
        }

        if (spec.backupCache) {
            val cacheItems = items.filter {
                it.path.startsWith(LocalPath.build(appInfo.dataDir, "cache").path)
            }

            val cacheRef: MMRef = mmDataRepo.create(MMRef.Request(
                    backupId = builder.backupId,
                    source = APathArchiveSource(localGateway, LocalPath.build(appInfo.dataDir), cacheItems)
            ))
            builder.putType(AppBackupWrap.Type.CACHE_PRIVATE_PRIMARY, listOf(cacheRef))
        }

        // TODO copy public data

        // TODO copy public cache

        // TODO copy public clutter

        return builder.createUnit()
    }

    override fun close() {
        keepAliveToken?.close()
    }

    override fun toString(): String = "AppEndpoint()"

    companion object {
        val TAG = App.logTag("Backup", "App", "BackupEndpoint")
    }
}