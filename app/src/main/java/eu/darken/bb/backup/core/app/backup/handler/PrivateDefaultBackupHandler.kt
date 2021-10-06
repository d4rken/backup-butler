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
import eu.darken.bb.common.*
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.*
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.archive.ArchiveRefSource
import eu.darken.bb.task.core.results.LogEvent
import io.reactivex.rxjava3.core.Observable
import timber.log.Timber
import javax.inject.Inject

@Reusable
class PrivateDefaultBackupHandler @Inject constructor(
    @ApplicationContext context: Context,
    private val gatewaySwitch: GatewaySwitch,
    private val mmDataRepo: MMDataRepo
) : BaseBackupHandler(context) {

    private val progressPub = HotData(tag = TAG) { Progress.Data() }
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override val keepAlive = SharedHolder.createKeepAlive(TAG)

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

    override fun backup(
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

        gatewaySwitch.keepAliveWith(this)

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

    private fun doBackup(
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
        gatewaySwitch.keepAlive.get().use {
            targets.forEach { target ->
                updateProgressSecondary(target.toCaString())
                target.walk(gatewaySwitch)
                    .filterNot { it == target }
                    .map { it.lookup(gatewaySwitch) }
                    .forEach { items.add(it) }
            }
        }

        items.forEach {
            Timber.tag(TAG).d("Adding to backup: %s", it)
            logListener?.invoke(LogEvent(LogEvent.Type.BACKUPPED, it))
        }

        val ref = mmDataRepo.create(
            MMRef.Request(
                backupId = backupId,
                source = ArchiveRefSource(
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