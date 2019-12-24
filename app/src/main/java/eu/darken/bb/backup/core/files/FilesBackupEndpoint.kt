package eu.darken.bb.backup.core.files

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.files.core.*
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.file.APathRefResource
import eu.darken.bb.task.core.results.LogEvent
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class FilesBackupEndpoint @Inject constructor(
        @AppContext override val context: Context,
        private val mmDataRepo: MMDataRepo,
        private val gatewaySwitch: GatewaySwitch
) : Backup.Endpoint, Progress.Client, HasContext {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    private val resourceTokens = mutableMapOf<APath.PathType, SharedHolder.Resource<*>>()

    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override fun backup(spec: BackupSpec, logListener: ((LogEvent) -> Unit)?): Backup.Unit {
        spec as FilesBackupSpec

        updateProgressPrimary(R.string.progress_creating_backup)
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        val gateway = gatewaySwitch.getGateway(spec.path)
        if (!resourceTokens.containsKey(spec.path.pathType)) {
            resourceTokens[spec.path.pathType] = gateway.keepAlive.get()
        }
        val builder = backupFile(spec, gateway, logListener)

        return builder.createUnit()
    }

    override fun close() {
        resourceTokens.values.forEach { it.close() }
    }

    private fun backupFile(spec: FilesBackupSpec, gateway: APathGateway<APath, APathLookup<APath>>, logListener: ((LogEvent) -> Unit)?): FilesBackupWrap {
        val builder = FilesBackupWrap(spec, Backup.Id())
        val pathToBackup = spec.path
        val items: List<APath> = gateway.keepAlive.get().use {
            pathToBackup.walk(gateway)
                    .filterNot { it == pathToBackup }
                    .onEach { Timber.tag(TAG).v("To backup: %s", it) }
                    .toList()
        }

        updateProgressCount(Progress.Count.Counter(0, items.size))

        val filesInUnit = mutableListOf<MMRef>()
        for (item in items) {
            updateProgressSecondary(item.path)

            val refRequest = MMRef.Request(
                    backupId = builder.backupId,
                    source = APathRefResource(gateway, item)
            )
            val ref = mmDataRepo.create(refRequest)
            filesInUnit.add(ref)
            logListener?.invoke(LogEvent(LogEvent.Type.BACKUPPED, item))

            updateProgressCount(Progress.Count.Counter(items.indexOf(item) + 1, items.size))
        }
        builder.files = filesInUnit
        return builder
    }

    companion object {
        val TAG = App.logTag("Backup", "Files", "BackupEndpoint")
    }
}