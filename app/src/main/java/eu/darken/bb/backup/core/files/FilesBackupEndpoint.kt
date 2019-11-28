package eu.darken.bb.backup.core.files

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
import eu.darken.bb.common.file.core.local.walkTopDown
import eu.darken.bb.common.file.core.saf.SAFGateway
import eu.darken.bb.common.file.core.saf.SAFPath
import eu.darken.bb.common.file.core.saf.walkTopDown
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.PathRefResource
import eu.darken.bb.processor.core.mm.SAFPathRefSource
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class FilesBackupEndpoint @Inject constructor(
        @AppContext override val context: Context,
        private val mmDataRepo: MMDataRepo,
        private val safGateway: SAFGateway,
        private val localGateway: LocalGateway
) : Backup.Endpoint, Progress.Client, HasContext {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    private var resourceToken: SharedResource.Resource<LocalGateway>? = null

    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override fun backup(spec: BackupSpec): Backup.Unit {
        spec as FilesBackupSpec

        updateProgressPrimary(R.string.progress_creating_backup_label)
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        if (resourceToken == null) resourceToken = localGateway.sharedResource.get()

        val builder = if (spec.path is SAFPath) {
            backupSAF(spec)
        } else {
            backupFile(spec)
        }

        return builder.createUnit()
    }

    override fun close() {
//        TODO("not implemented")
        resourceToken?.close()
    }

    private fun backupFile(spec: FilesBackupSpec): FilesBackupWrapper {
        val builder = FilesBackupWrapper(spec, Backup.Id())
        val pathToBackup = spec.path as LocalPath

        val resource = localGateway.sharedResource.get()
        val items = pathToBackup.walkTopDown(localGateway)
                .filterNot { it == pathToBackup }
                .onEach {
                    Timber.tag(TAG).v("To backup: %s", it)
                }
                .toList()
        resource.close()

        updateProgressCount(Progress.Count.Counter(0, items.size))

        for (item in items) {
            updateProgressSecondary(item.path)

            val refRequest = MMRef.Request(
                    backupId = builder.backupId,
                    source = PathRefResource(item, localGateway),
                    props = MMRef.Props(
                            originalPath = item,
                            dataType = if (item.isDirectory(localGateway)) MMRef.Type.DIRECTORY else MMRef.Type.FILE
                    )
            )
            val ref = mmDataRepo.create(refRequest)
            builder.files.add(ref)

            updateProgressCount(Progress.Count.Counter(items.indexOf(item) + 1, items.size))
        }
        return builder
    }

    private fun backupSAF(spec: FilesBackupSpec): FilesBackupWrapper {
        val builder = FilesBackupWrapper(spec, Backup.Id())
        val pathToBackup = spec.path as SAFPath

        val items: List<SAFPath> = pathToBackup.walkTopDown(safGateway)
                .filterNot { it == pathToBackup }
                .onEach {
                    Timber.tag(TAG).v("To backup: %s", it)
                }
                .toList()

        updateProgressCount(Progress.Count.Counter(0, items.size))

        for (item in items) {
            updateProgressSecondary(item.path)

            val refRequest = MMRef.Request(
                    backupId = builder.backupId,
                    source = SAFPathRefSource(item, safGateway),
                    props = MMRef.Props(
                            originalPath = item,
                            dataType = if (item.isDirectory(safGateway)) MMRef.Type.DIRECTORY else MMRef.Type.FILE
                    )
            )

            val ref = mmDataRepo.create(refRequest)
            builder.files.add(ref)

            updateProgressCount(Progress.Count.Counter(items.indexOf(item) + 1, items.size))
        }
        return builder
    }


    companion object {
        val TAG = App.logTag("Backup", "Files", "BackupEndpoint")
    }
}