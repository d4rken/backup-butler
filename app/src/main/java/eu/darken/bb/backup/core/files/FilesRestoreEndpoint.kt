package eu.darken.bb.backup.core.files

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.files.core.*
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.MMRef.Type.DIRECTORY
import eu.darken.bb.processor.core.mm.generic.DirectoryProps
import eu.darken.bb.processor.core.mm.generic.FileProps
import eu.darken.bb.processor.core.mm.generic.SymlinkProps
import eu.darken.bb.task.core.results.LogEvent
import io.reactivex.rxjava3.core.Observable
import timber.log.Timber
import javax.inject.Inject

class FilesRestoreEndpoint @Inject constructor(
        @AppContext override val context: Context,
        private val gateway: GatewaySwitch
) : Restore.Endpoint, Progress.Client, HasContext {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override val keepAlive = SharedHolder.createKeepAlive(TAG)

    override fun restore(config: Restore.Config, backup: Backup.Unit, logListener: ((LogEvent) -> Unit)?) {
        updateProgressPrimary(R.string.progress_restoring_backup)
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        config as FilesRestoreConfig
        val spec = backup.spec as FilesBackupSpec
        val handler = FilesBackupWrap(backup)

        updateProgressCount(Progress.Count.Counter(0, handler.files.size))

        gateway.keepAliveWith(this)

        // Dirs first, for stuff like SAFPath's we can't just do path.parent.mkdir()
        val toRestore = handler.files.sortedByDescending { it.props.dataType == DIRECTORY }

        for (ref in toRestore) {
            requireNotNull(ref.props.originalPath) { "Endpoint expects refs with an original path: $ref" }
            requireNotNull(ref.source) { "Invalid restore: config=$config, spec=$spec, ref=$ref" }

            updateProgressSecondary(ref.props.originalPath!!.path)

            restore(config, spec, ref, logListener)

            updateProgressCount(Progress.Count.Counter(handler.files.indexOf(ref) + 1, handler.files.size))
        }
    }

    private fun restore(
            config: FilesRestoreConfig,
            spec: FilesBackupSpec,
            ref: MMRef,
            logListener: ((LogEvent) -> Unit)?
    ) {
        val restorePath = config.restorePath ?: spec.path
        val chunks = spec.path.crumbsTo(ref.props.originalPath!!)
        val itemFile = restorePath.child(*chunks)

        if (itemFile.exists(gateway) && !config.replaceFiles) {
            Timber.tag(TAG).d("Skipping existing: %s", itemFile)
            return
        }

        when (ref.props) {
            is FileProps -> {
                itemFile.createFileIfNecessary(gateway)
                ref.source.open().copyToAutoClose(itemFile.write(gateway))
            }
            is DirectoryProps -> {
                itemFile.createDirIfNecessary(gateway)
            }
            is SymlinkProps -> {
                itemFile.createSymlink(gateway, (ref.props as SymlinkProps).symlinkTarget)
            }
        }
        logListener?.invoke(LogEvent(LogEvent.Type.RESTORED, restorePath))

        itemFile.setMetaData(gateway, ref.props)
    }

    companion object {
        val TAG = App.logTag("Backup", "Files", "RestoreEndpoint")
    }
}