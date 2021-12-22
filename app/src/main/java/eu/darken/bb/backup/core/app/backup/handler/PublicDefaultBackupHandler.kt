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
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.getString
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.sharedresource.SharedResource
import eu.darken.bb.common.toCaString
import eu.darken.bb.common.user.UserManagerBB
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
class PublicDefaultBackupHandler @Inject constructor(
    @ApplicationContext context: Context,
    private val gateway: GatewaySwitch,
    private val mmDataRepo: MMDataRepo,
    private val pkgOps: PkgOps,
    private val userManagerBB: UserManagerBB,
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

    override suspend fun backup(
        type: DataType,
        backupId: Backup.Id,
        spec: AppBackupSpec,
        appInfo: ApplicationInfo,
        builder: AppBackupWrap,
        target: APath?,
        logListener: ((LogEvent) -> Unit)?
    ) {
        when (type) {
            DataType.DATA_PUBLIC_PRIMARY, DataType.DATA_PUBLIC_SECONDARY -> updateProgressPrimary(R.string.progress_backingup_app_data)
            DataType.DATA_SDCARD_PRIMARY, DataType.DATA_SDCARD_SECONDARY -> updateProgressPrimary(R.string.progress_backingup_app_data)
            DataType.CACHE_PUBLIC_PRIMARY, DataType.CACHE_PUBLIC_SECONDARY -> updateProgressPrimary(R.string.progress_backingup_app_cache)
            DataType.CACHE_SDCARD_PRIMARY, DataType.CACHE_SDCARD_SECONDARY -> updateProgressPrimary(R.string.progress_backingup_app_cache)
            else -> throw UnsupportedOperationException("Can't restore $type")
        }
        updateProgressSecondary(CaString.EMPTY)
        updateProgressCount(Progress.Count.Indeterminate())

        gateway.addParent(this)
        pkgOps.addParent(this)

        try {
            val result = doBackup(type, backupId, spec, appInfo, logListener)
            builder.putDataType(type, result)
        } catch (e: Exception) {
            Timber.tag(TAG).e(
                e,
                "backup(type=%s, backupId=%s, spec=%s, appInfo=%s) failed",
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
        val currentUser = userManagerBB.currentUser

        val targetPairs: List<Pair<APath, Collection<APath>>> = when (type) {
            DataType.DATA_PUBLIC_PRIMARY -> {
                pkgOps.getPathInfos(appInfo.packageName, currentUser).publicPrimary.let { pubDataDir ->
                    val targets = if (pubDataDir.exists(gateway)) {
                        pubDataDir.listFiles(gateway).filter { !isCache(it) }
                    } else {
                        emptyList()
                    }
                    listOf(pubDataDir to targets)
                }
            }
            DataType.DATA_PUBLIC_SECONDARY -> {
                pkgOps.getPathInfos(appInfo.packageName, currentUser).publicSecondary.map { secStor ->
                    val targets = if (secStor.exists(gateway)) {
                        secStor.listFiles(gateway).filter { !isCache(it) }
                    } else {
                        emptyList()
                    }
                    secStor to targets
                }
            }
            DataType.CACHE_PUBLIC_PRIMARY -> {
                pkgOps.getPathInfos(appInfo.packageName, currentUser).publicPrimary.let { pubDataDir ->
                    val targets = if (pubDataDir.exists(gateway)) {
                        pubDataDir.listFiles(gateway).filterNot { isCache(it) }
                    } else {
                        emptyList()
                    }
                    listOf(pubDataDir to targets)
                }
            }
            DataType.CACHE_PUBLIC_SECONDARY -> {
                pkgOps.getPathInfos(appInfo.packageName, currentUser).publicSecondary.map { secStor ->
                    val targets = if (secStor.exists(gateway)) {
                        secStor.listFiles(gateway).filterNot { isCache(it) }
                    } else {
                        emptyList()
                    }
                    secStor to targets
                }
            }
            DataType.DATA_SDCARD_PRIMARY -> {
                TODO()
            }
            DataType.DATA_SDCARD_SECONDARY -> {
                TODO()
            }
            DataType.CACHE_SDCARD_PRIMARY -> {
                TODO()
            }
            DataType.CACHE_SDCARD_SECONDARY -> {
                TODO()
            }
            else -> throw UnsupportedOperationException("Can't restore $type")
        }

        val refs = mutableListOf<MMRef>()

        targetPairs.forEach { (storageBase, subdirs) ->
            val collectedSubDirContent = mutableListOf<APathLookup<APath>>()
            subdirs.forEach { target ->
                updateProgressSecondary(target.toCaString())
                Timber.tag(TAG).v("Walking: %s", target)

                gateway.sharedResource.get().use {
                    target.walk(gateway)
                        .filterNot { it == target }
                        .collect { collectedSubDirContent.add(it) }
                }
            }
            collectedSubDirContent.forEach {
                Timber.tag(TAG).d("Adding to backup: %s", it)
                logListener?.invoke(LogEvent(LogEvent.Type.BACKUPPED, it))
            }
            val mmRef = mmDataRepo.create(
                MMRef.Request(
                    backupId = backupId,
                    source = ArchiveRefSource.create(
                        gateway = gateway,
                        label = getString(type.labelRes),
                        archivePath = storageBase,
                        targets = collectedSubDirContent
                    )
                )
            )
            refs.add(mmRef)
        }

        return refs
    }

    private fun isCache(path: APath): Boolean {
        return path.name == "cache"
    }

    companion object {
        val TAG = logTag("Backup", "App", "Backup", "PublicDefaultBackupHandler")
    }
}