package eu.darken.bb.backup.core.app.restore

import android.content.Context

abstract class BaseRestoreHandler(context: Context) : RestoreHandler {
    override val priority: Int = 1
}