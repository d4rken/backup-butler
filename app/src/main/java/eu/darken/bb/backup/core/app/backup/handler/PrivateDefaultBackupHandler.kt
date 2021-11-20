package eu.darken.bb.backup.core.app.backup.handler

import android.content.Context
import android.content.pm.ApplicationInfo
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap
import eu.darken.bb.backup.core.app.AppBackupWrap.DataType
import eu.darken.bb.backup.core.app.backup.BaseBackupHandler
import eu.darken.bb.common.CaString
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.*
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.getString
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.sharedresource.SharedResource
import eu.darken.bb.common.toCaString
import eu.darken.bb.processor.core.ProcessorScope
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.archive.ArchiveRefSource
import eu.darken.bb.task.core.results.LogEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject

@Reusable
class PrivateDefaultBackupHandler @Inject constructor(
    @ApplicationContext context: Context,
    private val gatewaySwitch: GatewaySwitch,
    private val mmDataRepo: MMDataRepo,
    @ProcessorScope private val coroutineScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) : BaseBackupHandler(context) {

    private val progressPub = DynamicStateFlow(TAG, coroutineScope) { Progress.Data() }
    override val progress: Flow<Progress.Data> = progressPub.flow
    override fun updateProgress(update: suspend (Progress.Data) -> Progress.Data) = progressPub.updateAsync(onUpdate = update)

    override val sharedResource = SharedResource.createKeepAlive(TAG, coroutineScope + dispatcherProvider.IO)

    override fun isResponsible(
        type: DataType,
        config: AppBackupSpec,
        appInfo: ApplicationInfo,
        target: APath?
    ): Boolean {
        when (type) {
            DataType.DATA_PRIVATE_PRIMARY,
            DataType.CACHE_PRIVATE_PRIMARY -> {
                // It's okay
            }
            else -> return false
        }
        return true
    }

    override suspend fun backup(
        type: DataType,
        backupId: Backup.Id,
        spec: AppBackupSpec,
        appInfo: ApplicationInfo,
        wrap: AppBackupWrap,
        target: APath?,
        logListener: ((LogEvent) -> Unit)?
    ) {
        when (type) {
            DataType.DATA_PRIVATE_PRIMARY -> updateProgressPrimary(R.string.progress_backingup_app_data)
            DataType.CACHE_PRIVATE_PRIMARY -> updateProgressPrimary(R.string.progress_backingup_app_cache)
            else -> throw UnsupportedOperationException("Can't restore $type")
        }
        updateProgressSecondary(CaString.EMPTY)
        updateProgressCount(Progress.Count.Indeterminate())

        gatewaySwitch.addParent(this)

        try {
            val result = doBackup(type, backupId, spec, appInfo, logListener)
            wrap.putDataType(type, result)
        } catch (e: Exception) {
            Timber.tag(TAG).e(
                e, "backup(type=%s, backupId=%s, spec=%s, appInfo=%s) failed",
                type, backupId, spec, appInfo
            )
            throw e
        }
    }

    private suspend fun doBackup(
        type: DataType,
        backupId: Backup.Id,
        spec: AppBackupSpec,
        appInfo: ApplicationInfo,
        logListener: ((LogEvent) -> Unit)?
    ): Collection<MMRef> {
        updateProgressSecondary(appInfo.dataDir)

        val privDir = LocalPath.build(appInfo.dataDir)
        if (!privDir.exists(gatewaySwitch)) return emptyList()

        val targets = when (type) {
            DataType.DATA_PRIVATE_PRIMARY -> {
                privDir.listFiles(gatewaySwitch).filter { isNonCache(it) }
            }
            DataType.CACHE_PRIVATE_PRIMARY -> {
                privDir.listFiles(gatewaySwitch).filterNot { isNonCache(it) }
            }
            else -> throw UnsupportedOperationException("Can't backup $type")
        }

        val items = mutableListOf<APathLookup<APath>>()

        gatewaySwitch.sharedResource.get().use {
            targets.forEach { target ->
                updateProgressSecondary(target.toCaString())
                target.walk(gatewaySwitch)
                    .filterNot { it == target }
                    .collect { items.add(it) }
            }
        }

        items.forEach {
            Timber.tag(TAG).d("Adding to backup: %s", it)
            logListener?.invoke(LogEvent(LogEvent.Type.BACKUPPED, it))
        }

        val ref = mmDataRepo.create(
            MMRef.Request(
                backupId = backupId,
                source = ArchiveRefSource.create(
                    gatewaySwitch,
                    getString(type.labelRes),
                    privDir,
                    items
                )
            )
        )
        return listOf(ref)
    }

    private fun isNonCache(path: APath): Boolean {
        if (path.name == "cache") return false
        if (path.name == "code_cache") return false

        return true
    }


    companion object {
        val TAG = logTag("Backup", "App", "Backup", "PrivateDefaultBackupHandler")
    }
}