package eu.darken.bb.task.ui.editor.restore.config

import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.lists.*
import eu.darken.bb.common.ui.SwitchPreferenceView
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.ui.toggleGone
import eu.darken.bb.common.ui.updateExpander
import javax.inject.Inject


class RestoreConfigAdapter @Inject constructor()
    : ModularAdapter<RestoreConfigAdapter.BaseVH>(), AsyncAutoDataAdapter<ConfigUIWrap> {

    override val asyncDiffer: AsyncDiffer<ConfigUIWrap> = AsyncDiffer(
            this,
            compareItem = { i1, i2 -> i1.stableId == i2.stableId },
            compareContent = { i1, i2 -> i1.config == i2.config }
    )

    override fun getItemId(position: Int): Long = data[position].stableId

    override fun getItemCount(): Int = data.size

    init {
        setHasStableIds(true)
        modules.add(DataBinderModule<ConfigUIWrap, BaseVH>(data))
        modules.add(TypedVHCreator(0, { data[it].config is AppRestoreConfig }) { AppConfigVH(it) })
        modules.add(TypedVHCreator(1, { data[it].config is FilesRestoreConfig }) { FileConfigVH(it) })
    }

    abstract class BaseVH(@LayoutRes layoutRes: Int, parent: ViewGroup)
        : ModularAdapter.VH(layoutRes, parent), BindableVH<ConfigUIWrap> {

        private val defaultTag = getString(R.string.label_default)
        abstract val title: String

        @BindView(R.id.card_title) lateinit var cardTitle: TextView
        @BindView(R.id.card_subtitle) lateinit var cardSubTitle: TextView
        @BindView(R.id.header_container) lateinit var headerContainer: ViewGroup
        @BindView(R.id.header_toggle) lateinit var headerToggle: ImageView
        @BindView(R.id.options_container) lateinit var optionsContainer: ViewGroup

        @CallSuper
        override fun bind(item: ConfigUIWrap) {
            cardTitle.text = title
            cardSubTitle.text = when {
                item.isDefaultItem -> defaultTag
                item.backupInfo != null -> item.backupInfo.spec.getLabel(context)
                else -> getString(R.string.progress_loading_label)
            }
            if (item.isCustomConfig && !item.isDefaultItem) cardTitle.append("*")

            optionsContainer.setGone(!item.isDefaultItem && !item.isCustomConfig)
            headerToggle.updateExpander(optionsContainer)
            headerContainer.setOnClickListener {
                optionsContainer.toggleGone()
                headerToggle.updateExpander(optionsContainer)
            }
        }
    }

    class AppConfigVH(parent: ViewGroup)
        : BaseVH(R.layout.task_editor_restore_configs_adapter_line_app, parent) {

        @BindView(R.id.option_skip_existing_apps) lateinit var optionSkipExisting: SwitchPreferenceView
        @BindView(R.id.option_restore_apk) lateinit var optionRestoreApk: SwitchPreferenceView
        @BindView(R.id.option_restore_data) lateinit var optionRestoreData: SwitchPreferenceView

        override val title = getString(R.string.app_options_label)

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: ConfigUIWrap) {
            super.bind(item)
            val config = item.config as AppRestoreConfig

            optionSkipExisting.isChecked = config.skipExistingApps
            optionSkipExisting.setOnCheckedChangedListener { _, checked ->
                item.updateConfig(config.copy(skipExistingApps = checked))
            }
            optionRestoreApk.isChecked = config.restoreApk
            optionRestoreApk.setOnCheckedChangedListener { _, checked ->
                item.updateConfig(config.copy(restoreApk = checked))
            }
            optionRestoreData.isChecked = config.restoreData
            optionRestoreData.setOnCheckedChangedListener { _, checked ->
                item.updateConfig(config.copy(restoreData = checked))
            }
        }

    }

    class FileConfigVH(parent: ViewGroup)
        : BaseVH(R.layout.task_editor_restore_configs_adapter_line_files, parent) {

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
                    optionPathInfo.setText(R.string.storage_additional_permission_required_msg)
                    optionPathAction.setText(R.string.action_grant)
                } else {
                    optionPathInfo.text = ""
                    optionPathAction.setText(R.string.action_change)
                }

                if (config.restorePath != null) {
                    optionPathDisplay.text = config.restorePath.userReadablePath(context)
                } else {
                    optionPathDisplay.text = item.configWrap.defaultPath?.userReadablePath(context)
                }
            }
            optionPathContainer.setGone(item.isDefaultItem)

            optionReplaceExisting.isChecked = config.replaceFiles
            optionReplaceExisting.setOnCheckedChangedListener { _, checked ->
                item.updateConfig(config.copy(replaceFiles = checked))
            }
        }

    }
}