package eu.darken.bb.backup.core.app.backup.handler

import android.content.Context
import android.content.pm.ApplicationInfo
import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap.Type
import eu.darken.bb.backup.core.app.backup.BaseBackupHandler
import eu.darken.bb.common.AString
import eu.darken.bb.common.HotData
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.files.core.GatewaySwitch
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.files.core.lookup
import eu.darken.bb.common.files.core.walk
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.archive.APathArchiveSource
import eu.darken.bb.task.core.results.IOEvent
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

@Reusable
class PrivateDefaultBackupHandler @Inject constructor(
        @AppContext context: Context,
        private val gatewaySwitch: GatewaySwitch,
        private val mmDataRepo: MMDataRepo
) : BaseBackupHandler(context) {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override fun isResponsible(type: Type, config: AppBackupSpec, appInfo: ApplicationInfo): Boolean {
        when (type) {
            Type.DATA_PRIVATE_PRIMARY,
            Type.CACHE_PRIVATE_PRIMARY -> {
                // It's okay
            }
            else -> return false
        }
        return true
    }

    override fun backup(
            type: Type,
            backupId: Backup.Id,
            spec: AppBackupSpec,
            appInfo: ApplicationInfo,
            logListener: ((IOEvent) -> Unit)?
    ): Collection<MMRef> {
        when (type) {
            Type.DATA_PRIVATE_PRIMARY -> updateProgressPrimary(R.string.progress_backingup_app_data)
            Type.CACHE_PRIVATE_PRIMARY -> updateProgressPrimary(R.string.progress_backingup_app_cache)
            else -> throw UnsupportedOperationException("Can't restore $type")
        }
        updateProgressSecondary(AString.EMPTY)
        updateProgressCount(Progress.Count.Indeterminate())


        return try {
            doBackup(type, backupId, spec, appInfo, logListener)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "backup(type=%s, backupId=%s, spec=%s, appInfo=%s) failed",
                    type, backupId, spec, appInfo)
            throw e
        }
    }

    private fun doBackup(
            type: Type,
            backupId: Backup.Id,
            spec: AppBackupSpec,
            appInfo: ApplicationInfo,
            logListener: ((IOEvent) -> Unit)?
    ): Collection<MMRef> {
        updateProgressSecondary(appInfo.dataDir)
        // TODO split the walking? cache/non cache stuff?, extra mmrefs?
        val walkedPath = LocalPath.build(appInfo.dataDir)
        val items = gatewaySwitch.keepAlive.get().use {
            walkedPath.walk(gatewaySwitch)
                    .filterNot { it == walkedPath }
                    .onEach { Timber.tag(TAG).v("To backup: %s", it) }
                    .map { it.lookup(gatewaySwitch) }
                    .toList()
        }

        val filteredItems = when (type) {
            Type.DATA_PRIVATE_PRIMARY -> {
                items.filterNot {
                    it.path.startsWith(LocalPath.build(appInfo.dataDir, "cache").path)
                }
            }
            Type.CACHE_PRIVATE_PRIMARY -> {
                items.filter {
                    it.path.startsWith(LocalPath.build(appInfo.dataDir, "cache").path)
                }
            }
            else -> throw UnsupportedOperationException("Can't restore $type")
        }

//        filteredItems.forEach { logListener(it) }

        val dataRef: MMRef = mmDataRepo.create(MMRef.Request(
                backupId = backupId,
                source = APathArchiveSource(gatewaySwitch, LocalPath.build(appInfo.dataDir), filteredItems)
        ))
        return listOf(dataRef)
    }


    companion object {
        val TAG = App.logTag("Backup", "App", "Backup", "PrivateDataDefault")
    }
}