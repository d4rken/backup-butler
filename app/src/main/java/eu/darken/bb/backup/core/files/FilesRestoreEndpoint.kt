package eu.darken.bb.backup.core.files

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.core.copyToAutoClose
import eu.darken.bb.common.file.core.crumbsTo
import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.file.core.saf.SAFGateway
import eu.darken.bb.common.file.core.saf.SAFPath
import eu.darken.bb.common.file.core.saf.tryCreateFile
import eu.darken.bb.common.file.core.saf.tryMkDirs
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.MMRef.Type.DIRECTORY
import eu.darken.bb.processor.core.mm.MMRef.Type.FILE
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class FilesRestoreEndpoint @Inject constructor(
        @AppContext override val context: Context,
        private val safGateway: SAFGateway
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
        val handler = FilesBackupWrapper(backup)

        updateProgressCount(Progress.Count.Counter(0, handler.files.size))

        for (ref in handler.files) {
            requireNotNull(ref.props.originalPath) { "Endpoint expects refs with an original path: $ref" }
            requireNotNull(ref.source) { "Invalid restore: config=$config, spec=$spec, ref=$ref" }

            updateProgressSecondary(ref.props.originalPath.path)

            val restorePath = config.restorePath ?: spec.path

            if (restorePath is SAFPath) {
                restoreSAF(config, spec, ref, restorePath)
            } else {
                restoreFile(config, spec, ref, restorePath as LocalPath)
            }

            updateProgressCount(Progress.Count.Counter(handler.files.indexOf(ref) + 1, handler.files.size))
        }

        return true
    }

    override fun close() {
//        TODO("not implemented")
    }

    private fun restoreFile(config: FilesRestoreConfig, spec: FilesBackupSpec, ref: MMRef, restorePath: LocalPath) {
        val chunks = spec.path.crumbsTo(ref.props.originalPath!!)
        // TODO root support
        val itemFile = restorePath.child(*chunks).file
        if (itemFile.exists() && !config.replaceFiles) {
            Timber.tag(TAG).d("Skipping existing: %s", itemFile)
            return
        }
        when (ref.props.dataType) {
            FILE -> {
                itemFile.parentFile.mkdirs()
                ref.source!!.open().copyToAutoClose(itemFile)
            }
            DIRECTORY -> {
                itemFile.mkdirs()
            }
        }
    }

    private fun restoreSAF(config: FilesRestoreConfig, spec: FilesBackupSpec, ref: MMRef, restorePath: SAFPath) {
        val chunks = spec.path.crumbsTo(ref.props.originalPath!!)
        val itemFile = restorePath.child(*chunks)

        if (itemFile.exists(safGateway) && !config.replaceFiles) {
            Timber.tag(TAG).d("Skipping existing: %s", itemFile)
            return
        }

        when (ref.props.dataType) {
            FILE -> {
                itemFile.tryCreateFile(safGateway)
                ref.source!!.open().copyToAutoClose(safGateway.write(itemFile))
            }
            DIRECTORY -> {
                itemFile.tryMkDirs(safGateway)
            }
        }
    }

    companion object {
        val TAG = App.logTag("Backup", "Files", "RestoreEndpoint")
    }
}