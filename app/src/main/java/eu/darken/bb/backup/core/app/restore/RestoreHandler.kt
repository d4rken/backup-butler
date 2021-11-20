package eu.darken.bb.backup.core.app.restore

import android.content.pm.ApplicationInfo
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.sharedresource.HasSharedResource
import eu.darken.bb.task.core.results.LogEvent

interface RestoreHandler : Progress.Client, Progress.Host, HasSharedResource<Any> {

    val priority: Int

    fun isResponsible(type: AppBackupWrap.DataType, config: AppRestoreConfig, spec: AppBackupSpec): Boolean

    suspend fun restore(
        type: AppBackupWrap.DataType,
        appInfo: ApplicationInfo,
        config: AppRestoreConfig,
        wrap: AppBackupWrap,
        logListener: ((LogEvent) -> Unit)? = null
    )
}