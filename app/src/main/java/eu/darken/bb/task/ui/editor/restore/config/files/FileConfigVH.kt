package eu.darken.bb.task.ui.editor.restore.config.files

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.databinding.TaskEditorRestoreConfigsAdapterLineFilesBinding
import eu.darken.bb.task.ui.editor.restore.config.ConfigUIWrap
import eu.darken.bb.task.ui.editor.restore.config.RestoreConfigAdapter

class FileConfigVH(parent: ViewGroup) : RestoreConfigAdapter.BaseVH<TaskEditorRestoreConfigsAdapterLineFilesBinding>(
    R.layout.task_editor_restore_configs_adapter_line_files, parent
) {

    override val viewBinding: Lazy<TaskEditorRestoreConfigsAdapterLineFilesBinding> = lazy {
        TaskEditorRestoreConfigsAdapterLineFilesBinding.bind(itemView)
    }
    override val onBindData: TaskEditorRestoreConfigsAdapterLineFilesBinding.(
        item: ConfigUIWrap,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        container.setConfigWrap(
            getString(R.string.task_editor_restore_config_file_options_label),
            item,
        )
        item as FilesConfigUIWrap

        val config = item.config as FilesRestoreConfig

        if (!item.isDefaultItem) {
            optionPathAction.setOnClickListener { item.runPathAction() }
            optionPathInfo.setGone(item.filesConfig.isPermissionGranted)

            if (!item.filesConfig.isPermissionGranted) {
                optionPathInfo.setTextColor(context.getColorForAttr(R.attr.colorError))
                optionPathInfo.text = getString(
                    R.string.general_error_cant_access_msg, item.filesConfig.currentPath?.userReadablePath(context)
                    ?: "?"
                )
            } else {
                optionPathInfo.setTextColor(context.getColorForAttr(R.attr.colorOnBackground))
                optionPathInfo.text = ""
            }
            optionPathAction.setText(R.string.general_change_action)

            if (config.restorePath != null) {
                optionPathDisplay.text = config.restorePath.userReadablePath(context)
            } else {
                optionPathDisplay.text = item.filesConfig.defaultPath?.userReadablePath(context)
            }
        }
        optionPath.setGone(item.isDefaultItem)

        if (item.filesConfig.isPermissionGranted) {
            container.subTitle.setTextColor(context.getColorForAttr(R.attr.colorOnBackground))
        } else {
            container.subTitle.setTextColor(context.getColorForAttr(R.attr.colorError))
        }

        optionReplaceExistingFiles.isChecked = config.replaceFiles
        optionReplaceExistingFiles.setSwitchListener { _, checked ->
            item.updateConfig(config.copy(replaceFiles = checked))
        }
    }
}