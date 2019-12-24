package eu.darken.bb.backup.core.app.backup

import android.content.pm.ApplicationInfo
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.task.core.results.LogEvent

interface BackupHandler : Progress.Client, Progress.Host {

    val priority: Int

    fun isResponsible(type: AppBackupWrap.Type, config: AppBackupSpec, appInfo: ApplicationInfo): Boolean

    fun backup(
            type: AppBackupWrap.Type,
            backupId: Backup.Id,
            spec: AppBackupSpec,
            appInfo: ApplicationInfo,
            logListener: ((LogEvent) -> Unit)? = null
    ): Collection<MMRef>

}