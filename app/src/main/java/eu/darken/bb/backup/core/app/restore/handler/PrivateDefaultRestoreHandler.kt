package eu.darken.bb.backup.core.app.restore.handler

import android.content.Context
import android.content.pm.ApplicationInfo
import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap
import eu.darken.bb.backup.core.app.AppBackupWrap.DataType
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.app.restore.BaseRestoreHandler
import eu.darken.bb.common.AString
import eu.darken.bb.common.HotData
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.files.core.*
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.task.core.results.LogEvent
import io.reactivex.Observable
import okio.source
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@Reusable
class PrivateDefaultRestoreHandler @Inject constructor(
        @AppContext context: Context,
        private val gatewaySwitch: GatewaySwitch,
        private val pkgOps: PkgOps
) : BaseRestoreHandler(context) {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override fun isResponsible(type: DataType, config: AppRestoreConfig, spec: AppBackupSpec): Boolean {
        when (type) {
            DataType.DATA_PRIVATE_PRIMARY,
            DataType.CACHE_PRIVATE_PRIMARY -> {
                // It's okay
            }
            else -> return false
        }
        return true
    }

    override fun restore(
            appInfo: ApplicationInfo,
            config: AppRestoreConfig,
            type: DataType,
            wrap: AppBackupWrap,
            logListener: ((LogEvent) -> Unit)?
    ) {
        when (type) {
            DataType.DATA_PRIVATE_PRIMARY -> updateProgressPrimary(R.string.progress_restoring_app_data)
            DataType.CACHE_PRIVATE_PRIMARY -> updateProgressPrimary(R.string.progress_restoring_app_cache)
            else -> throw UnsupportedOperationException("Can't restore $type")
        }
        updateProgressSecondary(AString.EMPTY)
        updateProgressCount(Progress.Count.Indeterminate())

        val toRestore = wrap.getDataType(type)

        for ((index, archive) in toRestore.withIndex()) {
            try {
                doRestore(appInfo, config, type, archive, logListener)
                updateProgressCount(Progress.Count.Percent(index + 1, toRestore.size))
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "restore(pkg=%s, config=%s, toRestore=%s) failed", appInfo.packageName, config, toRestore)
                throw e
            }
        }
    }

    private fun doRestore(
            appInfo: ApplicationInfo,
            config: AppRestoreConfig,
            type: DataType,
            archive: MMRef,
            logListener: ((LogEvent) -> Unit)?
    ) {

        val directoryTimeStamps = mutableMapOf<APath, Date>()

        val props = archive.props
        archive.source.open().use { source ->
            val archiveStream = TarArchiveInputStream(GzipCompressorInputStream(source.inputStream()))

            generateSequence { archiveStream.nextTarEntry }.forEach { entry ->
                val entryOrigPath = entry.name
                Timber.tag(TAG).v("Restoring: %s", entryOrigPath)
                updateProgressSecondary(entryOrigPath)

                val basePath = when (type) {
                    DataType.DATA_PRIVATE_PRIMARY -> appInfo.dataDir
                    DataType.CACHE_PRIVATE_PRIMARY -> appInfo.dataDir
                    else -> throw UnsupportedOperationException("Can't restore $type")
                }

                props.originalPath
                val restoreTarget = LocalPath.build(basePath, entryOrigPath)

                // TODO what if the file exists?
                when {
                    entry.isSymbolicLink -> {
                        // TODO
                    }
                    entry.isDirectory -> {
                        restoreTarget.createDirIfNecessary(gatewaySwitch)
                    }
                    entry.isFile -> {
                        restoreTarget.createFileIfNecessary(gatewaySwitch)
                        archiveStream.source().constrain(entry.size).use { fileSource ->
                            restoreTarget.write(gatewaySwitch).use {
                                fileSource.copyToAutoClose(it)
                            }
                        }
                    }
                    else -> throw UnsupportedOperationException("Unknown type for ${entry.name}")
                }
                logListener?.invoke(LogEvent(LogEvent.Type.RESTORED, restoreTarget))

                val modifiedTime = entry.lastModifiedDate
                if (entry.isDirectory) {
                    directoryTimeStamps[restoreTarget] = modifiedTime
                } else {
                    restoreTarget.setModifiedAt(gatewaySwitch, modifiedTime)
                }

                val permissions = Permissions(entry.mode)
                restoreTarget.setPermissions(gatewaySwitch, permissions)

                val targetUID = appInfo.uid
                var targetGID: Int? = null

                val oldOwnership = entry.getOwnership()
                if (oldOwnership?.groupName?.endsWith("_cache") == true) {
                    val targetUIDName = pkgOps.getUserNameForUID(targetUID)
                    if (targetUIDName != null) {
                        val targetGIDName = targetUIDName + "_cache"
                        targetGID = pkgOps.getGIDForGroupName(targetGIDName)
                    }
                }
                if (targetGID == null) targetGID = targetUID

                restoreTarget.setOwnership(gatewaySwitch, Ownership(targetUID, targetGID))
            }
        }

        Timber.tag(TAG).v("Setting timestamps for %d directories.", directoryTimeStamps.size)
        updateProgressSecondary(R.string.progress_fixing_timestamps)
        directoryTimeStamps.forEach { (path, lastModified) ->
            path.setModifiedAt(gatewaySwitch, lastModified)
        }
    }

    companion object {
        val TAG = App.logTag("Backup", "App", "Restore", "PrivateDataDefault")
    }
}