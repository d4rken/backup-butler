package eu.darken.bb.backup.core.files

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.asFile
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.mm.MMRef
import io.reactivex.Observable
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class FilesRestoreEndpoint @Inject constructor(
        @AppContext override val context: Context
) : Restore.Endpoint, Progress.Client, HasContext {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data

    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override fun restore(config: Restore.Config, backup: Backup.Unit): Boolean {
        updateProgressPrimary(R.string.progress_restoring_backup)
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        config as FilesRestoreConfig
        val spec = backup.spec as FilesBackupSpec
        val restoreDir = File(spec.path.asFile().parent, spec.path.name + "-1")
        val handler = FilesBackupBuilder(backup)

        updateProgressCount(Progress.Count.Counter(0, handler.files.size))

        for (ref in handler.files) {
            updateProgressSecondary(ref.originalPath.path)

            val itemPath = ref.originalPath.path.replace(spec.path.path, restoreDir.path)
            val itemFile = File(itemPath)
            if (itemFile.exists() && !config.replaceFiles) {
                Timber.tag(TAG).d("Skipping existing: %s", itemFile)
                continue
            }
            when (ref.type) {
                MMRef.Type.FILE -> {
                    itemFile.parentFile.mkdirs()
                    ref.tmpPath.copyTo(itemFile)
                }
                MMRef.Type.DIRECTORY -> {
                    itemFile.mkdirs()
                }
            }

            updateProgressCount(Progress.Count.Counter(handler.files.indexOf(ref) + 1, handler.files.size))
        }

        return true
    }

    companion object {
        val TAG = App.logTag("Backup", "Files", "RestoreEndpoint")
    }
}