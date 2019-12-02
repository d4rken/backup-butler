package eu.darken.bb.backup.core.files

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.SharedResource
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.core.*
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.MMRef.Type.*
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class FilesRestoreEndpoint @Inject constructor(
        @AppContext override val context: Context,
        private val gatewaySwitch: GatewaySwitch
) : Restore.Endpoint, Progress.Client, HasContext {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    private val resourceTokens = mutableMapOf<APath.PathType, SharedResource.Resource<*>>()

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

            updateProgressSecondary(ref.props.originalPath!!.path)

            val gateway = gatewaySwitch.getGateway(spec.path)
            if (!resourceTokens.containsKey(spec.path.pathType)) {
                resourceTokens[spec.path.pathType] = gateway.resourceTokens.get()
            }

            // TODO add restore success to results?
            restore(config, spec, ref, gateway)

            updateProgressCount(Progress.Count.Counter(handler.files.indexOf(ref) + 1, handler.files.size))
        }

        return true
    }

    override fun close() {
        resourceTokens.values.forEach { it.close() }
    }

    private fun restore(config: FilesRestoreConfig, spec: FilesBackupSpec, ref: MMRef, gateway: APathGateway<APath, APathLookup<APath>>) {
        val restorePath = config.restorePath ?: spec.path
        val chunks = spec.path.crumbsTo(ref.props.originalPath!!)
        val itemFile = restorePath.child(*chunks)

        if (itemFile.exists(gateway) && !config.replaceFiles) {
            Timber.tag(TAG).d("Skipping existing: %s", itemFile)
            return
        }
        // TODO check success ? add to results?
        when (ref.props.dataType) {
            FILE -> {
                itemFile.createFileIfNecessary(gateway)
                ref.source.open().copyToAutoClose(itemFile.write(gateway))
            }
            DIRECTORY -> {
                itemFile.createDirIfNecessary(gateway)
            }
            SYMBOLIC_LINK -> {
                itemFile.createSymlink(gateway, ref.props.symlinkTarget!!)
            }
        }
        itemFile.setMetaData(gateway, ref.props)
    }

    companion object {
        val TAG = App.logTag("Backup", "Files", "RestoreEndpoint")
    }
}