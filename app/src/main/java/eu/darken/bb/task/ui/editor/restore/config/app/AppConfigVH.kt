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

    override val viewBinding: Lazy<TaskEditorRestoreConfigsAdapterLineAppBinding> = lazy {
        TaskEditorRestoreConfigsAdapterLineAppBinding.bind(itemView)
    }

    override val onBindData: TaskEditorRestoreConfigsAdapterLineAppBinding.(
        item: ConfigUIWrap,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        item as AppConfigUIWrap
        val config = item.config as AppRestoreConfig

        container.setConfigWrap(getString(R.string.task_editor_restore_app_config_options_label), item)

        optionSkipExistingApps.apply {
            isChecked = config.skipExistingApps
            setSwitchListener { _, checked ->
                item.updateConfig(config.copy(skipExistingApps = checked))
            }
        }
        optionRestoreApk.apply {
            isChecked = config.restoreApk
            setSwitchListener { _, checked ->
                item.updateConfig(config.copy(restoreApk = checked))
            }
        }
        optionRestoreData.apply {
            isChecked = config.restoreData
            setSwitchListener { _, checked ->
                item.updateConfig(config.copy(restoreData = checked))
            }
            isEnabled = item.isRooted
            description = getString(
                if (!item.isRooted) R.string.root_rooted_device_required_label
                else R.string.task_editor_restore_app_config_restore_data_desc
            )
        }
        optionRestoreCache.apply {
            isChecked = config.restoreCache
            setSwitchListener { _, checked ->
                item.updateConfig(config.copy(restoreCache = checked))
            }
            isEnabled = item.isRooted
            description = getString(
                if (!item.isRooted) R.string.root_rooted_device_required_label
                else R.string.task_editor_restore_app_config_restore_cache_desc
            )
        }
    }
}