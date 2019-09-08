package eu.darken.bb.backup.core.files

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
import eu.darken.bb.common.file.asSFile
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import timber.log.Timber

class FilesBackupEndpoint @AssistedInject constructor(
        @Assisted private val progressClient: Progress.Client?,
        @AppContext override val context: Context,
        private val MMDataRepo: MMDataRepo
) : Backup.Endpoint, Progress.Client, HasContext {

    override fun updateProgress(update: (Progress.Data) -> Progress.Data) {
        progressClient?.updateProgress(update)
    }

    override fun backup(spec: BackupSpec): Backup.Unit {
        updateProgressPrimary(R.string.progress_creating_backup)
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        spec as FilesBackupSpec
        val builder = FilesBackupBuilder(spec, Backup.Id())

        val pathToBackup = spec.path.asFile()

        val items = pathToBackup.walkTopDown()
                .filterNot { it == pathToBackup }
                .onEach {
                    Timber.tag(TAG).v("To backup: %s", it)
                }
                .toList()

        updateProgressCount(Progress.Count.Counter(0, items.size))

        for (item in items) {
            updateProgressSecondary(item.path)

            val ref: MMRef = MMDataRepo.create(
                    backupId = builder.backupId,
                    orig = item.asSFile()
            )
            item.copyTo(ref.tmpPath)
            builder.files.add(ref)

            updateProgressCount(Progress.Count.Counter(items.indexOf(item) + 1, items.size))
        }

        return builder.toBackup()
    }

    companion object {
        val TAG = App.logTag("Backup", "Files", "BackupEndpoint")
    }


    @AssistedInject.Factory
    interface Factory : Backup.Endpoint.Factory<FilesBackupEndpoint>
}