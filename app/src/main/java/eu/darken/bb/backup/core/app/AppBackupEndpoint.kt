package eu.darken.bb.backup.core.app

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.mm.MMDataRepo
import io.reactivex.Observable
import javax.inject.Inject

class AppBackupEndpoint @Inject constructor(
        @AppContext override val context: Context,
        private val mmDataRepo: MMDataRepo,
        private val apkExporter: APKExporter
) : Backup.Endpoint, Progress.Client, HasContext {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override fun backup(spec: BackupSpec): Backup.Unit {
        spec as AppBackupSpec
        val builder = AppBackupWrapper(spec, Backup.Id())
        updateProgressPrimary(R.string.progress_creating_backup_label)
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        val apkData = apkExporter.getAPKFile(spec.packageName)
        updateProgressCount(Progress.Count.Counter(1, (apkData.splitSources.size + 1)))

        updateProgressSecondary(apkData.mainSource.path)
        // FIXME
        // TODO
//        val baseApkRef: MMRef = MMDataRepo.create(backupId = builder.backupId, orig = apkData.mainSource.asSFile())
//
//        apkData.mainSource.safeCopyTo(baseApkRef.tmpPath)
//        builder.baseApk = baseApkRef
//
//        val splitApkRefs = mutableListOf<MMRef>()
//        apkData.splitSources.forEach { splitApk ->
//            updateProgressSecondary(splitApk.path)
//            updateProgressCount(Progress.Count.Counter(apkData.splitSources.indexOf(splitApk) + 2, (apkData.splitSources.size + 2)))
//
//            val splitSourceRef = MMDataRepo.create(backupId = builder.backupId, orig = splitApk.asSFile())
//            splitApk.safeCopyTo(splitSourceRef.tmpPath)
//            splitApkRefs.add(splitSourceRef)
//        }
//        builder.splitApks = splitApkRefs

        return builder.createUnit()
    }

    override fun close() {
//        TODO("not implemented")
    }


    override fun toString(): String = "AppEndpoint()"

    companion object {
        val TAG = App.logTag("Backup", "App", "BackupEndpoint")
    }
}