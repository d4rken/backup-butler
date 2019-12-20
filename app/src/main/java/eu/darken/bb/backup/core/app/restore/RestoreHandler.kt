package eu.darken.bb.backup.core.app.restore

import android.content.pm.ApplicationInfo
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.app.AppBackupWrap
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.common.files.core.APath

interface RestoreHandler {

    val priority: Int

    fun isResponsible(type: AppBackupWrap.Type, config: AppRestoreConfig, spec: AppBackupSpec): Boolean

    fun restore(
            appInfo: ApplicationInfo,
            config: AppRestoreConfig,
            type: AppBackupWrap.Type,
            wrap: AppBackupWrap
    ): Result

    data class Result(
            val items: Collection<APath>,
            val error: Exception? = null
    )
}