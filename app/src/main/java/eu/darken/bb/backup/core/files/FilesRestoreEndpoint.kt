package eu.darken.bb.backup.core.files

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.*
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.sharedresource.SharedResource
import eu.darken.bb.processor.core.ProcessorScope
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.Props
import eu.darken.bb.processor.core.mm.generic.DirectoryProps
import eu.darken.bb.processor.core.mm.generic.FileProps
import eu.darken.bb.processor.core.mm.generic.SymlinkProps
import eu.darken.bb.task.core.results.LogEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.plus
import javax.inject.Inject

class FilesRestoreEndpoint @Inject constructor(
    @ApplicationContext override val context: Context,
    private val gateway: GatewaySwitch,
    @ProcessorScope private val processorScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) : Restore.Endpoint, Progress.Client, HasContext {

    private val progressPub = DynamicStateFlow(TAG, processorScope) { Progress.Data() }
    override val progress: Flow<Progress.Data> = progressPub.flow
    override fun updateProgress(update: suspend (Progress.Data) -> Progress.Data) = progressPub.updateAsync(onUpdate = update)

    override val sharedResource = SharedResource.createKeepAlive(TAG, processorScope + dispatcherProvider.IO)

    override suspend fun restore(config: Restore.Config, backup: Backup.Unit, logListener: ((LogEvent) -> Unit)?) {
        log(TAG) { "restore(config=$config, backup=$backup) starting..." }

        updateProgressPrimary(R.string.progress_restoring_backup)
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        config as FilesRestoreConfig
        val spec = backup.spec as FilesBackupSpec
        val handler = FilesBackupWrap(backup)

        updateProgressCount(Progress.Count.Counter(0, handler.files.size))

        gateway.addParent(this)

        // Dirs first, for stuff like SAFPath's we can't just do path.parent.mkdir()
        val toRestore = handler.files
            .map { it to it.getProps() }
            .sortedWith(
                compareByDescending<Pair<MMRef, Props>> { (_, props) -> props.dataType == MMRef.Type.DIRECTORY }
                    .thenBy { (_, props) -> props.originalPath?.segments?.size ?: 1 }
            )
            .map { it.first }

        val delayedActions = mutableListOf<suspend () -> Unit>()

        for (ref in toRestore) {
            log(TAG) { "Restoring ${ref.getProps()}" }
            requireNotNull(ref.getProps().originalPath) { "Endpoint expects refs with an original path: $ref" }
            requireNotNull(ref.source) { "Invalid restore: config=$config, spec=$spec, ref=$ref" }

            updateProgressSecondary(ref.getProps().originalPath!!.path)

            val delayedAction = restore(config, spec, ref, logListener)
            if (delayedAction != null) delayedActions.add(delayedAction)

            updateProgressCount(Progress.Count.Counter(handler.files.indexOf(ref) + 1, handler.files.size))
        }
        log(TAG) { "Fixing directory meta data" }

        delayedActions.forEach { it.invoke() }

        log(TAG) { "restore(...) done." }
    }

    private suspend fun restore(
        config: FilesRestoreConfig,
        spec: FilesBackupSpec,
        ref: MMRef,
        logListener: ((LogEvent) -> Unit)?
    ): (suspend () -> Unit)? {
        val restorePath = config.restorePath ?: spec.path
        val chunks = spec.path.crumbsTo(ref.getProps().originalPath!!)
        val itemFile = restorePath.child(*chunks)

        if (itemFile.exists(gateway) && !config.replaceFiles) {
            log(TAG) { "Skipping existing: $itemFile" }
            return null
        }

        // Creating directories as part of the file creation means they already exist when we read a DirectoryProps
        // If we process DirectoryProps first, then add files, the lastModified time is wrong
        // So we need to set directory metadata last
        var delayedAction: (suspend () -> Unit)? = null

        when (ref.getProps()) {
            is FileProps -> {
                itemFile.createFileIfNecessary(gateway)
                ref.source.open().copyToAutoClose(itemFile.write(gateway))
                itemFile.setMetaData(gateway, ref.getProps())
            }
            is DirectoryProps -> {
                itemFile.createDirIfNecessary(gateway)
                delayedAction = { itemFile.setMetaData(gateway, ref.getProps()) }
            }
            is SymlinkProps -> {
                itemFile.createSymlink(gateway, (ref.getProps() as SymlinkProps).symlinkTarget)
                itemFile.setMetaData(gateway, ref.getProps())
            }
        }

        logListener?.invoke(LogEvent(LogEvent.Type.RESTORED, restorePath))

        return delayedAction
    }

    companion object {
        val TAG = logTag("Backup", "Files", "RestoreEndpoint")
    }
}