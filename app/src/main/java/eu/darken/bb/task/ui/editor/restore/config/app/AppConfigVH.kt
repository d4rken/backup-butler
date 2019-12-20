package eu.darken.bb.task.ui.editor.restore.config.app

import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.common.ui.SwitchPreferenceView
import eu.darken.bb.task.ui.editor.restore.config.ConfigUIWrap
import eu.darken.bb.task.ui.editor.restore.config.RestoreConfigAdapter

class AppConfigVH(parent: ViewGroup)
    : RestoreConfigAdapter.BaseVH(R.layout.task_editor_restore_configs_adapter_line_app, parent) {

    @BindView(R.id.option_skip_existing_apps) lateinit var optionSkipExisting: SwitchPreferenceView
    @BindView(R.id.option_restore_apk) lateinit var optionRestoreApk: SwitchPreferenceView
    @BindView(R.id.option_restore_data) lateinit var optionRestoreData: SwitchPreferenceView
    @BindView(R.id.option_restore_cache) lateinit var optionRestoreCache: SwitchPreferenceView

    override val title = getString(R.string.task_editor_restore_app_config_options_label)

    init {
        ButterKnife.bind(this, itemView)
    }

    override fun bind(item: ConfigUIWrap) {
        super.bind(item)
        val config = item.config as AppRestoreConfig

        optionSkipExisting.isChecked = config.skipExistingApps
        optionSkipExisting.setSwitchListener { _, checked ->
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