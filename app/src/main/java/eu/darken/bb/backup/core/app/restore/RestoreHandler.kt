package eu.darken.bb.backup.core.app.restore

import android.content.pm.ApplicationInfo
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.task.core.results.IOEvent

interface RestoreHandler : Progress.Client, Progress.Host {

    val priority: Int

    fun isResponsible(type: AppBackupWrap.Type, config: AppRestoreConfig, spec: AppBackupSpec): Boolean

    fun restore(
            appInfo: ApplicationInfo,
            config: AppRestoreConfig,
            type: AppBackupWrap.Type,
            wrap: AppBackupWrap,
            logListener: ((IOEvent) -> Unit)? = null
    )
}