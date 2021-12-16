package eu.darken.bb.task.ui.editor.restore.config.files

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import eu.darken.bb.task.ui.editor.restore.config.ConfigUIWrap

data class FilesConfigUIWrap(
    val filesConfig: SimpleRestoreTaskEditor.FilesConfigWrap,
    val configCallback: (Restore.Config, Backup.Id?) -> Unit,
    val pathAction: ((SimpleRestoreTaskEditor.FilesConfigWrap, Backup.Id?) -> Unit)?
) : ConfigUIWrap(filesConfig, configCallback) {

    fun runPathAction() = pathAction?.invoke(filesConfig, filesConfig.backupInfoOpt!!.backupId)

}