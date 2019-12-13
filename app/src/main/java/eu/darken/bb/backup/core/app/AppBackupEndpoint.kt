package eu.darken.bb.backup.core.app

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.SharedResource
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.core.local.LocalGateway
import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.file.core.walk
import eu.darken.bb.common.pkgs.IPCFunnel
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
        private val ipcFunnel: IPCFunnel,
        private val mmDataRepo: MMDataRepo,
        private val apkExporter: APKExporter,
        private val localGateway: LocalGateway
) : Backup.Endpoint, Progress.Client, HasContext {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    private var gatewayToken: SharedResource.Resource<*>? = null

    override fun backup(spec: BackupSpec): Backup.Unit {
        spec as AppBackupSpec
        val builder = AppBackupWrapper(spec, Backup.Id())
        updateProgressPrimary(R.string.progress_creating_backup_label)
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        if (spec.backupApk) {
            val apkData = apkExporter.getAPKFile(spec.packageName)
            updateProgressCount(Progress.Count.Counter(1, (apkData.splitSources.size + 1)))

            updateProgressSecondary(apkData.mainSource.path)

            val baseApkRef: MMRef = mmDataRepo.create(MMRef.Request(
                    backupId = builder.backupId,
                    source = FileRefSource(apkData.mainSource)
            ))

            builder.baseApk = baseApkRef

            val splitApkRefs = mutableListOf<MMRef>()
            apkData.splitSources.forEach { splitApk ->
                updateProgressSecondary(splitApk.path)
                updateProgressCount(Progress.Count.Counter(apkData.splitSources.indexOf(splitApk) + 2, (apkData.splitSources.size + 2)))

                val splitRef: MMRef = mmDataRepo.create(MMRef.Request(
                        backupId = builder.backupId,
                        source = FileRefSource(splitApk)
                ))
                splitApkRefs.add(splitRef)
            }
            builder.splitApks = splitApkRefs
        }

        val appInfo = ipcFunnel.submit(IPCFunnel.AppInfoQuery(spec.packageName))
        requireNotNull(appInfo) { "Unable to lookup ${spec.packageName}" }

        // TODO root stuff, only when enabled?
        if (gatewayToken == null) gatewayToken = localGateway.resourceTokens.get()
        val items = (LocalPath.build(appInfo.dataDir)).walk(localGateway)
                .onEach { Timber.tag(TAG).v("To backup: %s", it) }
                .map { it.lookup(localGateway) }
                .toList()

        // Private data
        if (spec.backupData) {
            val nonCacheItems = items.filterNot {
                it.path.startsWith(LocalPath.build(appInfo.dataDir, "cache").path)
            }

            val dataRef: MMRef = mmDataRepo.create(MMRef.Request(
                    backupId = builder.backupId,
                    source = APathArchiveSource(localGateway, LocalPath.build(appInfo.dataDir), nonCacheItems)
            ))
            builder.dataPrivate = mutableListOf(dataRef)
        }

        if (spec.backupCache) {
            val cacheItems = items.filter {
                it.path.startsWith(LocalPath.build(appInfo.dataDir, "cache").path)
            }

            val dataRef: MMRef = mmDataRepo.create(MMRef.Request(
                    backupId = builder.backupId,
                    source = APathArchiveSource(localGateway, LocalPath.build(appInfo.dataDir), cacheItems)
            ))
            builder.dataPrivate = mutableListOf(dataRef)
        }

        // TODO copy public data

        // TODO copy public cache

        // TODO copy public clutter

        return builder.createUnit()
    }

    override fun close() {
        gatewayToken?.close()
    }

    override fun toString(): String = "AppEndpoint()"

    companion object {
        val TAG = App.logTag("Backup", "App", "BackupEndpoint")
    }
}