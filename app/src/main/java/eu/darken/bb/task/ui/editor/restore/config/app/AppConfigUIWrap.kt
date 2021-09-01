package eu.darken.bb.task.ui.editor.restore.config.app

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import eu.darken.bb.task.ui.editor.restore.config.ConfigUIWrap

data class AppConfigUIWrap(
    val filesConfig: SimpleRestoreTaskEditor.AppsConfigWrap,
    val updateConfigCallback: (Restore.Config, Backup.Id?) -> Unit
) : ConfigUIWrap(filesConfig, updateConfigCallback)