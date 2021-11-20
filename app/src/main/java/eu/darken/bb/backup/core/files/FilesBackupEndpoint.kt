package eu.darken.bb.backup.core.files

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.APathLookup
import eu.darken.bb.common.files.core.GatewaySwitch
import eu.darken.bb.common.files.core.walk
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.sharedresource.HasSharedResource
import eu.darken.bb.common.sharedresource.SharedResource
import eu.darken.bb.processor.core.ProcessorScope
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.generic.GenericRefSource
import eu.darken.bb.task.core.results.LogEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject

class FilesBackupEndpoint @Inject constructor(
    @ApplicationContext override val context: Context,
    private val mmDataRepo: MMDataRepo,
    private val gatewaySwitch: GatewaySwitch,
    @ProcessorScope private val processorScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) : Backup.Endpoint, Progress.Client, HasContext, HasSharedResource<Any> {

    private val progressPub = DynamicStateFlow(TAG, processorScope) { Progress.Data() }
    override val progress: Flow<Progress.Data> = progressPub.flow
    override fun updateProgress(update: suspend (Progress.Data) -> Progress.Data) = progressPub.updateAsync(onUpdate = update)


    override val sharedResource = SharedResource.createKeepAlive(TAG, processorScope + dispatcherProvider.IO)

    override suspend fun backup(spec: BackupSpec, logListener: ((LogEvent) -> Unit)?): Backup.Unit {
        spec as FilesBackupSpec

        updateProgressPrimary(R.string.progress_creating_backup)
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        gatewaySwitch.addParent(this)

        val builder = backupFile(spec, gatewaySwitch, logListener)

        return builder.createUnit()
    }

    private suspend fun backupFile(
        spec: FilesBackupSpec,
        gatewaySwitch: GatewaySwitch,
        logListener: ((LogEvent) -> Unit)?
    ): FilesBackupWrap {
        val builder = FilesBackupWrap(spec, Backup.Id())
        val pathToBackup = spec.path

        val items: List<APathLookup<APath>> = gatewaySwitch.sharedResource.get().use {
            pathToBackup.walk(gatewaySwitch)
                .filterNot { it == pathToBackup }
                .toList()
        }

        updateProgressCount(Progress.Count.Counter(0, items.size))

        val filesInUnit = mutableListOf<MMRef>()
        for (item in items) {
            updateProgressSecondary(item.path)

            val refRequest = MMRef.Request(
                backupId = builder.backupId,
                source = GenericRefSource.create(
                    gateway = gatewaySwitch,
                    path = item
                )
            )
            val ref = mmDataRepo.create(refRequest)
            filesInUnit.add(ref)
            logListener?.invoke(LogEvent(LogEvent.Type.BACKUPPED, item))
            Timber.tag(TAG).d("Adding to backup: %s", item)

            updateProgressCount(Progress.Count.Counter(items.indexOf(item) + 1, items.size))
        }
        builder.files = filesInUnit
        return builder
    }

    companion object {
        val TAG = logTag("Backup", "Files", "BackupEndpoint")
    }
}