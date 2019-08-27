package eu.darken.bb.backup.core.app

import android.content.Context
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.asFile
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.tmp.TmpDataRepo
import eu.darken.bb.processor.core.tmp.TmpRef

class AppBackupEndpoint @AssistedInject constructor(
        @Assisted val progressClient: Progress.Client?,
        @AppContext override val context: Context,
        private val tmpDataRepo: TmpDataRepo,
        private val apkExporter: APKExporter
) : Backup.Endpoint, Progress.Client, HasContext {

    override fun updateProgress(update: (Progress.Data) -> Progress.Data) {
        progressClient?.updateProgress(update)
    }

    override fun backup(spec: BackupSpec): Backup.Unit {
        spec as AppBackupSpec
        val builder = AppBackupBuilder(spec, Backup.Id())
        updateProgressPrimary(R.string.creating_backup)
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        val apkData = apkExporter.getAPKFile(spec.packageName)
        updateProgressCount(Progress.Count.Counter(1, (apkData.splitSources.size + 1)))

        updateProgressSecondary(apkData.mainSource.path)
        val baseApkRef: TmpRef = tmpDataRepo.create(backupId = builder.backupId)
        baseApkRef.originalPath = apkData.mainSource
        apkData.mainSource.asFile().copyTo(baseApkRef.file.asFile())
        builder.baseApk = baseApkRef

        val splitApkRefs = mutableListOf<TmpRef>()
        apkData.splitSources.forEach { splitApk ->
            updateProgressSecondary(splitApk.path)
            updateProgressCount(Progress.Count.Counter(apkData.splitSources.indexOf(splitApk) + 2, (apkData.splitSources.size + 2)))

            val splitSourceRef = tmpDataRepo.create(builder.backupId)
            splitSourceRef.originalPath = splitApk
            splitApk.asFile().copyTo(splitSourceRef.file.asFile())
            splitApkRefs.add(splitSourceRef)
        }
        builder.splitApks = splitApkRefs

        return builder.toBackup()
    }


    override fun toString(): String = "AppEndpoint()"

    companion object {
        val TAG = App.logTag("Backup", "App", "BackupEndpoint")
    }

    @AssistedInject.Factory
    interface Factory : Backup.Endpoint.Factory<AppBackupEndpoint>

}