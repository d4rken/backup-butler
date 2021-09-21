package eu.darken.bb.task.ui.editor.restore.config.app

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.databinding.TaskEditorRestoreConfigsAdapterLineAppBinding
import eu.darken.bb.task.ui.editor.restore.config.ConfigUIWrap
import eu.darken.bb.task.ui.editor.restore.config.RestoreConfigAdapter

class AppConfigVH(parent: ViewGroup) : RestoreConfigAdapter.BaseVH<TaskEditorRestoreConfigsAdapterLineAppBinding>(
    R.layout.task_editor_restore_configs_adapter_line_app, parent
) {

//    override val title =

    override val viewBinding: Lazy<TaskEditorRestoreConfigsAdapterLineAppBinding> = lazy {
        TaskEditorRestoreConfigsAdapterLineAppBinding.bind(itemView)
    }
    override val onBindData: TaskEditorRestoreConfigsAdapterLineAppBinding.(
        item: ConfigUIWrap,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        container.setConfigWrap(
            getString(R.string.task_editor_restore_app_config_options_label),
            item,
        )
        val config = item.config as AppRestoreConfig

        optionSkipExistingApps.isChecked = config.skipExistingApps
        optionSkipExistingApps.setSwitchListener { _, checked ->
            item.updateConfig(config.copy(skipExistingApps = checked))
        }
        optionRestoreApk.isChecked = config.restoreApk
        optionRestoreApk.setSwitchListener { _, checked ->
            item.updateConfig(config.copy(restoreApk = checked))
        }
        optionRestoreData.isChecked = config.restoreData
        optionRestoreData.setSwitchListener { _, checked ->
            item.updateConfig(config.copy(restoreData = checked))
        }
        optionRestoreCache.isChecked = config.restoreCache
        optionRestoreCache.setSwitchListener { _, checked ->
            item.updateConfig(config.copy(restoreCache = checked))
        }
    }
}