package eu.darken.bb.backup.core.app.backup

import android.content.pm.ApplicationInfo
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap
import eu.darken.bb.common.HasSharedResource
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.task.core.results.LogEvent

interface BackupHandler : Progress.Client, Progress.Host, HasSharedResource<Any> {

    val priority: Int

    fun isResponsible(
        type: AppBackupWrap.DataType,
        config: AppBackupSpec,
        appInfo: ApplicationInfo,
        target: APath?
    ): Boolean

    suspend fun backup(
        type: AppBackupWrap.DataType,
        backupId: Backup.Id,
        spec: AppBackupSpec,
        appInfo: ApplicationInfo,
        wrap: AppBackupWrap,
        target: APath?,
        logListener: ((LogEvent) -> Unit)?
    )

}