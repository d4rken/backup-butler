package eu.darken.bb.task.ui.editor.restore.config.app

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import eu.darken.bb.task.ui.editor.restore.config.ConfigUIWrap

data class AppConfigUIWrap(
    val appConfig: SimpleRestoreTaskEditor.AppsConfigWrap,
    val isRooted: Boolean,
    val updateConfigCallback: (Restore.Config, Backup.Id?) -> Unit
) : ConfigUIWrap(appConfig, updateConfigCallback)