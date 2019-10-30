package eu.darken.bb.task.ui.editor.restore.config.files

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import eu.darken.bb.task.ui.editor.restore.config.ConfigUIWrap

data class FilesConfigUIWrap(
        val configWrap: SimpleRestoreTaskEditor.FilesConfigWrap,
        val configCallback: (Restore.Config, Backup.Id?) -> Unit,
        val pathAction: ((SimpleRestoreTaskEditor.FilesConfigWrap, Backup.Id?) -> Unit)?
) : ConfigUIWrap(configWrap, configCallback) {

    fun runPathAction() = pathAction?.invoke(configWrap, configWrap.backupInfoOpt!!.backupId)

}