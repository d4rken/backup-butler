package eu.darken.bb.backup.core.app.restore

import android.content.pm.ApplicationInfo
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.task.core.results.LogEvent

interface RestoreHandler : Progress.Client, Progress.Host {

    val priority: Int

    fun isResponsible(type: AppBackupWrap.DataType, config: AppRestoreConfig, spec: AppBackupSpec): Boolean

    fun restore(
            appInfo: ApplicationInfo,
            config: AppRestoreConfig,
            type: AppBackupWrap.DataType,
            wrap: AppBackupWrap,
            logListener: ((LogEvent) -> Unit)? = null
    )
}