package eu.darken.bb.backup.core.app.backup

import android.content.Context

abstract class BaseBackupHandler(context: Context) : BackupHandler {
    override val priority: Int = 1
}