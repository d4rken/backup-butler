package eu.darken.bb.backup.core.files

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.*
import eu.darken.bb.common.file.SAFGateway
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.MMRef.Type.*
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject
import kotlin.io.copyTo

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
            updateProgressSecondary(ref.originalPath.path)

            val restorePath = config.restorePath ?: spec.path

            if (restorePath is SAFPath) {
                restoreSAF(config, spec, ref, restorePath)
            } else {
                restoreFile(config, spec, ref, restorePath as JavaPath)
            }

            updateProgressCount(Progress.Count.Counter(handler.files.indexOf(ref) + 1, handler.files.size))
        }

        return true
    }

    private fun restoreFile(config: FilesRestoreConfig, spec: FilesBackupSpec, ref: MMRef, restorePath: JavaPath) {
        val chunks = spec.path.crumbsTo(ref.originalPath)
        val itemFile = restorePath.child(*chunks).file
        if (itemFile.exists() && !config.replaceFiles) {
            Timber.tag(TAG).d("Skipping existing: %s", itemFile)
            return
        }
        when (ref.type) {
            FILE -> {
                itemFile.parentFile.mkdirs()
                ref.tmpPath.copyTo(itemFile)
            }
            DIRECTORY -> {
                itemFile.mkdirs()
            }
            UNUSED -> throw IllegalStateException("Ref is unused: ${ref.tmpPath}")
        }
    }

    private fun restoreSAF(config: FilesRestoreConfig, spec: FilesBackupSpec, ref: MMRef, restorePath: SAFPath) {
        val chunks = spec.path.crumbsTo(ref.originalPath)
        val itemFile = restorePath.child(*chunks)

        if (itemFile.exists(safGateway) && !config.replaceFiles) {
            Timber.tag(TAG).d("Skipping existing: %s", itemFile)
            return
        }

        when (ref.type) {
            FILE -> {
                itemFile.tryCreateFile(safGateway)
                safGateway.openFile(itemFile, SAFGateway.FileMode.WRITE) {
                    ref.tmpPath.copyTo(it)
                }
            }
            DIRECTORY -> {
                itemFile.tryMkDirs(safGateway)
            }
            UNUSED -> throw IllegalStateException("Ref is unused: ${ref.tmpPath}")
        }
    }

    companion object {
        val TAG = App.logTag("Backup", "Files", "RestoreEndpoint")
    }
}