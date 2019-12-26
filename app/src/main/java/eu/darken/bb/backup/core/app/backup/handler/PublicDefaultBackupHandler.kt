package eu.darken.bb.backup.core.app.backup.handler

import android.content.Context
import android.content.pm.ApplicationInfo
import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap
import eu.darken.bb.backup.core.app.AppBackupWrap.DataType
import eu.darken.bb.backup.core.app.backup.BaseBackupHandler
import eu.darken.bb.common.AString
import eu.darken.bb.common.HotData
import eu.darken.bb.common.PathAString
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.files.core.*
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.archive.ArchiveRefSource
import eu.darken.bb.task.core.results.LogEvent
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

@Reusable
class PublicDefaultBackupHandler @Inject constructor(
        @AppContext context: Context,
        private val gatewaySwitch: GatewaySwitch,
        private val mmDataRepo: MMDataRepo,
        private val pkgOps: PkgOps
) : BaseBackupHandler(context) {

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data
    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    // TODO use this keepAlive?
    override val keepAlive = SharedHolder.createKeepAlive(TAG)

    override fun isResponsible(type: DataType, config: AppBackupSpec, appInfo: ApplicationInfo): Boolean {
        when (type) {
            DataType.DATA_PUBLIC_PRIMARY,
            DataType.DATA_PUBLIC_SECONDARY,
            DataType.CACHE_PUBLIC_PRIMARY,
            DataType.CACHE_PUBLIC_SECONDARY -> {
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
            builder: AppBackupWrap,
            logListener: ((LogEvent) -> Unit)?
    ) {
        when (type) {
            DataType.DATA_PUBLIC_PRIMARY, DataType.DATA_PUBLIC_SECONDARY -> updateProgressPrimary(R.string.progress_backingup_app_data)
            DataType.CACHE_PUBLIC_PRIMARY, DataType.CACHE_PUBLIC_SECONDARY -> updateProgressPrimary(R.string.progress_backingup_app_cache)
            else -> throw UnsupportedOperationException("Can't restore $type")
        }
        updateProgressSecondary(AString.EMPTY)
        updateProgressCount(Progress.Count.Indeterminate())

        gatewaySwitch.keepAliveWith(this)

        try {
            val result = doBackup(type, backupId, spec, appInfo, logListener)
            builder.putDataType(type, result)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e,
                    "backup(type=%s, backupId=%s, spec=%s, appInfo=%s) failed",
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

        val targetPairs = mutableListOf<Pair<APath, Collection<APath>>>()
        when (type) {
            DataType.DATA_PUBLIC_PRIMARY -> {
                val pubDataDir = pkgOps.getPathInfos(appInfo.packageName).publicPrimary
                val targets = if (pubDataDir.exists(gatewaySwitch)) {
                    pubDataDir.listFiles(gatewaySwitch).filter { isNonCache(it) }
                } else {
                    emptyList()
                }
                targetPairs.add(Pair(pubDataDir, targets))
            }
            DataType.DATA_PUBLIC_SECONDARY -> {
                pkgOps.getPathInfos(appInfo.packageName).publicSecondary.forEach { secStor ->
                    val targets = if (secStor.exists(gatewaySwitch)) {
                        secStor.listFiles(gatewaySwitch).filter { isNonCache(it) }
                    } else {
                        emptyList()
                    }
                    targetPairs.add(Pair(secStor, targets))
                }
            }
            DataType.CACHE_PUBLIC_PRIMARY -> {
                val pubDataDir = pkgOps.getPathInfos(appInfo.packageName).publicPrimary
                val targets = if (pubDataDir.exists(gatewaySwitch)) {
                    pubDataDir.listFiles(gatewaySwitch).filterNot { isNonCache(it) }
                } else {
                    emptyList()
                }
                targetPairs.add(Pair(pubDataDir, targets))
            }
            DataType.CACHE_PUBLIC_SECONDARY -> {
                pkgOps.getPathInfos(appInfo.packageName).publicSecondary.forEach { secStor ->
                    val targets = if (secStor.exists(gatewaySwitch)) {
                        secStor.listFiles(gatewaySwitch).filterNot { isNonCache(it) }
                    } else {
                        emptyList()
                    }
                    targetPairs.add(Pair(secStor, targets))
                }
            }
            else -> throw UnsupportedOperationException("Can't restore $type")
        }

        val refs = mutableListOf<MMRef>()
        targetPairs.forEach { (storageBase, subdirs) ->
            val collectedSubDirContent = mutableListOf<APathLookup<APath>>()
            subdirs.forEach { target ->
                updateProgressSecondary(PathAString(target))
                Timber.tag(TAG).v("Walking: %s", target)

                gatewaySwitch.keepAlive.get().use {
                    target.walk(gatewaySwitch)
                            .filterNot { it == target }
                            .map { it.lookup(gatewaySwitch) }
                            .forEach { collectedSubDirContent.add(it) }
                }
            }
            collectedSubDirContent.forEach {
                Timber.tag(TAG).d("Adding to backup: %s", it)
                logListener?.invoke(LogEvent(LogEvent.Type.BACKUPPED, it))
            }
            val mmRef = mmDataRepo.create(MMRef.Request(
                    backupId = backupId,
                    source = ArchiveRefSource(gatewaySwitch, type.key, storageBase, collectedSubDirContent)
            ))
            refs.add(mmRef)
        }

        return refs
    }

    private fun isNonCache(path: APath): Boolean {
        if (path.name == "cache") return false

        return true
    }

    companion object {
        val TAG = App.logTag("Backup", "App", "Backup", "PublicDefaultBackupHandler")
    }
}