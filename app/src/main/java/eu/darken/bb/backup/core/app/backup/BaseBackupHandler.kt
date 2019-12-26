package eu.darken.bb.backup.core.app.backup

import android.content.Context
import eu.darken.bb.common.HasContext

abstract class BaseBackupHandler(override val context: Context) : BackupHandler, HasContext {
    override val priority: Int = 1
}