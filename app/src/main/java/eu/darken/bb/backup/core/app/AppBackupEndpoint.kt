package eu.darken.bb.backup.core.app

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.asSFile
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import io.reactivex.Observable
import javax.inject.Inject

class AppBackupEndpoint @Inject constructor(
        @AppContext override val context: Context,
        private val MMDataRepo: MMDataRepo,
        private val apkExporter: APKExporter
) : Backup.Endpoint, Progress.Client, HasContext {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override fun backup(spec: BackupSpec): Backup.Unit {
        spec as AppBackupSpec
        val builder = AppBackupBuilder(spec, Backup.Id())
        updateProgressPrimary(R.string.progress_creating_backup)
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        val apkData = apkExporter.getAPKFile(spec.packageName)
        updateProgressCount(Progress.Count.Counter(1, (apkData.splitSources.size + 1)))

        updateProgressSecondary(apkData.mainSource.path)
        val baseApkRef: MMRef = MMDataRepo.create(backupId = builder.backupId, orig = apkData.mainSource.asSFile())

        apkData.mainSource.copyTo(baseApkRef.tmpPath)
        builder.baseApk = baseApkRef

        val splitApkRefs = mutableListOf<MMRef>()
        apkData.splitSources.forEach { splitApk ->
            updateProgressSecondary(splitApk.path)
            updateProgressCount(Progress.Count.Counter(apkData.splitSources.indexOf(splitApk) + 2, (apkData.splitSources.size + 2)))

            val splitSourceRef = MMDataRepo.create(builder.backupId, splitApk.asSFile())
            splitApk.copyTo(splitSourceRef.tmpPath)
            splitApkRefs.add(splitSourceRef)
        }
        builder.splitApks = splitApkRefs

        // TODO set metadata?

        return builder.toBackup()
    }


    override fun toString(): String = "AppEndpoint()"

    companion object {
        val TAG = App.logTag("Backup", "App", "BackupEndpoint")
    }
}