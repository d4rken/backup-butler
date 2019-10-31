package eu.darken.bb.task.ui.editor.restore.config.files

import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.ui.SwitchPreferenceView
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.task.ui.editor.restore.config.ConfigUIWrap
import eu.darken.bb.task.ui.editor.restore.config.RestoreConfigAdapter

class FileConfigVH(parent: ViewGroup)
    : RestoreConfigAdapter.BaseVH(R.layout.task_editor_restore_configs_adapter_line_files, parent) {

    @BindView(R.id.option_replace_existing_files) lateinit var optionReplaceExisting: SwitchPreferenceView
    @BindView(R.id.option_path) lateinit var optionPathContainer: ViewGroup
    @BindView(R.id.option_path_display) lateinit var optionPathDisplay: TextView
    @BindView(R.id.option_path_info) lateinit var optionPathInfo: TextView
    @BindView(R.id.option_path_action) lateinit var optionPathAction: Button

    override val title = getString(R.string.file_options_label)

    init {
        ButterKnife.bind(this, itemView)
    }

    override fun bind(item: ConfigUIWrap) {
        super.bind(item)
        item as FilesConfigUIWrap

        val config = item.config as FilesRestoreConfig

        if (!item.isDefaultItem) {
            optionPathAction.setOnClickListener { item.runPathAction() }
            optionPathInfo.setGone(item.configWrap.isPermissionGranted)

            if (!item.configWrap.isPermissionGranted) {
                optionPathInfo.setTextColor(context.getColorForAttr(R.attr.colorError))
                optionPathInfo.text = getString(R.string.error_msg_cant_access, item.configWrap.currentPath?.userReadablePath(context)
                        ?: "?")
            } else {
                optionPathInfo.setTextColor(context.getColorForAttr(R.attr.colorOnBackground))
                optionPathInfo.text = ""
            }
            optionPathAction.setText(R.string.action_change)

            if (config.restorePath != null) {
                optionPathDisplay.text = config.restorePath.userReadablePath(context)
            } else {
                optionPathDisplay.text = item.configWrap.defaultPath?.userReadablePath(context)
            }
        }
        optionPathContainer.setGone(item.isDefaultItem)

        if (item.configWrap.isPermissionGranted) {
            cardSubTitle.setTextColor(context.getColorForAttr(R.attr.colorOnBackground))
        } else {
            cardSubTitle.setTextColor(context.getColorForAttr(R.attr.colorError))
        }

        optionReplaceExisting.isChecked = config.replaceFiles
        optionReplaceExisting.setSwitchListener { _, checked ->
            item.updateConfig(config.copy(replaceFiles = checked))
        }
    }

}