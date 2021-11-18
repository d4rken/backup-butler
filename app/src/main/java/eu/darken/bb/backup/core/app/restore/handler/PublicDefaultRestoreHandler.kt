package eu.darken.bb.backup.core.app.restore.handler

import android.content.Context
import android.content.pm.ApplicationInfo
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.R
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap
import eu.darken.bb.backup.core.app.AppBackupWrap.DataType
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.app.restore.BaseRestoreHandler
import eu.darken.bb.common.CaString
import eu.darken.bb.common.SharedResource
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.*
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.user.UserManagerBB
import eu.darken.bb.processor.core.ProcessorScope
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.Props
import eu.darken.bb.processor.core.mm.archive.ArchiveProps
import eu.darken.bb.processor.core.mm.archive.ArchiveRef
import eu.darken.bb.processor.core.mm.generic.SymlinkProps
import eu.darken.bb.task.core.results.LogEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.plus
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@Reusable
class PublicDefaultRestoreHandler @Inject constructor(
    @ApplicationContext context: Context,
    private val gateway: GatewaySwitch,
    private val pkgOps: PkgOps,
    private val userManager: UserManagerBB,
    @ProcessorScope private val processorScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) : BaseRestoreHandler(context) {

    private val progressPub = DynamicStateFlow(TAG, processorScope) { Progress.Data() }
    override val progress: Flow<Progress.Data> = progressPub.flow
    override fun updateProgress(update: suspend (Progress.Data) -> Progress.Data) = progressPub.updateAsync(onUpdate = update)

    override val sharedResource = SharedResource.createKeepAlive(TAG, processorScope + dispatcherProvider.IO)

    override fun isResponsible(type: DataType, config: AppRestoreConfig, spec: AppBackupSpec): Boolean {
        when (type) {
            DataType.DATA_PUBLIC_PRIMARY,
            DataType.DATA_PUBLIC_SECONDARY -> {
                // It's okay
            }
            DataType.CACHE_PUBLIC_PRIMARY,
            DataType.CACHE_PUBLIC_SECONDARY -> {
                // It's okay
            }
            DataType.DATA_SDCARD_PRIMARY,
            DataType.DATA_SDCARD_SECONDARY -> {
                // It's okay
            }
            DataType.CACHE_SDCARD_PRIMARY,
            DataType.CACHE_SDCARD_SECONDARY -> {
                // It's okay
            }
            else -> return false
        }
        return true
    }

    override suspend fun restore(
        type: DataType,
        appInfo: ApplicationInfo,
        config: AppRestoreConfig,
        wrap: AppBackupWrap,
        logListener: ((LogEvent) -> Unit)?
    ) {
        when (type) {
            DataType.DATA_PUBLIC_PRIMARY, DataType.DATA_PUBLIC_SECONDARY -> updateProgressPrimary(R.string.progress_restoring_app_data)
            DataType.DATA_SDCARD_PRIMARY, DataType.DATA_SDCARD_SECONDARY -> updateProgressPrimary(R.string.progress_restoring_app_data)
            DataType.CACHE_PUBLIC_PRIMARY, DataType.CACHE_PUBLIC_SECONDARY -> updateProgressPrimary(R.string.progress_restoring_app_cache)
            DataType.CACHE_SDCARD_PRIMARY, DataType.CACHE_SDCARD_SECONDARY -> updateProgressPrimary(R.string.progress_restoring_app_cache)
            else -> throw UnsupportedOperationException("Can't restore $type")
        }
        updateProgressSecondary(CaString.EMPTY)
        updateProgressCount(Progress.Count.Indeterminate())

        gateway.keepAliveWith(this)
        pkgOps.keepAliveWith(this)

        val currentUser = userManager.currentUser
        val toRestore = wrap.getDataType(type)

        for ((index, archive) in toRestore.withIndex()) {
            try {
                val basePath = when (type) {
                    DataType.DATA_PUBLIC_PRIMARY, DataType.CACHE_PUBLIC_PRIMARY -> {
                        pkgOps.getPathInfos(appInfo.packageName, currentUser).publicPrimary
                    }
                    DataType.DATA_PUBLIC_SECONDARY, DataType.CACHE_PUBLIC_SECONDARY -> {
                        // TODO support more than one secondary, needs matching though
                        pkgOps.getPathInfos(appInfo.packageName, currentUser).publicSecondary.firstOrNull()
                    }
                    DataType.DATA_SDCARD_PRIMARY, DataType.CACHE_SDCARD_PRIMARY -> {
                        TODO()
                    }
                    DataType.DATA_SDCARD_SECONDARY, DataType.CACHE_SDCARD_SECONDARY -> {
                        TODO()
                    }
                    else -> throw UnsupportedOperationException("Can't restore $type")
                }
                if (basePath == null) continue
                restorePath(type, appInfo, config, basePath, archive, logListener)
                updateProgressCount(Progress.Count.Percent(index + 1, toRestore.size))
            } catch (e: Exception) {
                Timber.tag(TAG)
                    .e(e, "restore(pkg=%s, config=%s, toRestore=%s) failed", appInfo.packageName, config, toRestore)
                throw e
            }
        }
    }

    private suspend fun restorePath(
        type: DataType,
        appInfo: ApplicationInfo,
        config: AppRestoreConfig,
        basePath: APath,
        archive: MMRef,
        logListener: ((LogEvent) -> Unit)?
    ) {

        val directoryTimeStamps = mutableMapOf<APath, Date>()

        val archiveProps = archive.getProps() as ArchiveProps
        Timber.tag(TAG).d("Restoring archive: %s", archiveProps)
        (archive.source as ArchiveRef).extract().collect { (itemProps, itemSource) ->
            Timber.tag(TAG).v("Restoring archive item: %s", itemProps)
            updateProgressSecondary(itemProps.tryLabel)

            val restoreTarget = LocalPath.build(basePath as LocalPath, itemProps.originalPath!!.path)

            if (!config.overwriteExisting && restoreTarget.exists(gateway)) {
                Timber.tag(TAG).d("Overwriting existing files is disabled, skipping past: %s", restoreTarget)
                return@collect
            }

            when (itemProps.dataType) {
                MMRef.Type.FILE -> {
                    restoreTarget.createFileIfNecessary(gateway)
                    itemSource!!.use { fileSource ->
                        restoreTarget.write(gateway).use {
                            fileSource.copyToAutoClose(it)
                        }
                    }
                }
                MMRef.Type.DIRECTORY -> {
                    restoreTarget.createDirIfNecessary(gateway)
                }
                MMRef.Type.SYMLINK -> {
                    itemProps as SymlinkProps
                    restoreTarget.createSymlink(gateway, itemProps.symlinkTarget)
                }
                else -> throw UnsupportedOperationException("Unsupported type $itemProps")
            }
            logListener?.invoke(LogEvent(LogEvent.Type.RESTORED, restoreTarget))

            if (itemProps is Props.HasModifiedDate) {
                if (itemProps.dataType == MMRef.Type.DIRECTORY) {
                    directoryTimeStamps[restoreTarget] = itemProps.modifiedAt
                } else {
                    restoreTarget.setModifiedAt(gateway, itemProps.modifiedAt)
                }
            }

            if (itemProps is Props.HasPermissions && itemProps.permissions != null) {
                restoreTarget.setPermissions(gateway, itemProps.permissions!!)
            }

            if (itemProps is Props.HasOwner) {
                val targetUID = appInfo.uid
                var targetGID: Int? = null

                val oldOwnership = itemProps.ownership
                if (oldOwnership?.groupName?.endsWith("_cache") == true) {
                    val targetUIDName = pkgOps.getUserNameForUID(targetUID)
                    if (targetUIDName != null) {
                        val targetGIDName = targetUIDName + "_cache"
                        targetGID = pkgOps.getGIDForGroupName(targetGIDName)
                    }
                }
                if (targetGID == null) targetGID = targetUID

                restoreTarget.setOwnership(gateway, Ownership(targetUID, targetGID))
            }
        }

        Timber.tag(TAG).v("Setting timestamps for %d directories.", directoryTimeStamps.size)
        updateProgressSecondary(R.string.progress_fixing_timestamps)
        directoryTimeStamps.forEach { (path, lastModified) ->
            path.setModifiedAt(gateway, lastModified)
        }
    }

    companion object {
        val TAG = logTag("Backup", "App", "Restore", "PublicDefaultRestoreHandler")
    }
}