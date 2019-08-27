package eu.darken.bb.task.ui.editor.restore.config

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.lists.*
import eu.darken.bb.common.ui.SwitchPreferenceView
import javax.inject.Inject


class RestoreConfigAdapter @Inject constructor()
    : ModularAdapter<RestoreConfigAdapter.BaseVH>(), DataAdapter<Pair<Restore.Config, (Restore.Config) -> Unit>> {

    override val data = mutableListOf<Pair<Restore.Config, (Restore.Config) -> Unit>>()

    init {
        modules.add(DataBinderModule<Pair<Restore.Config, (Restore.Config) -> Unit>, BaseVH>(data))
        modules.add(TypedVHCreator(0, { data[it].first is AppRestoreConfig }) { AppConfigVH(it) })
        modules.add(TypedVHCreator(1, { data[it].first is FilesRestoreConfig }) { FileConfigVH(it) })
    }

    override fun getItemCount(): Int = data.size


    abstract class BaseVH(@LayoutRes layoutRes: Int, parent: ViewGroup)
        : ModularAdapter.VH(layoutRes, parent), BindableVH<Pair<Restore.Config, (Restore.Config) -> Unit>>

    class AppConfigVH(parent: ViewGroup)
        : BaseVH(R.layout.task_editor_restore_configs_adapter_line_app, parent) {
        @BindView(R.id.option_skip_existing_apps) lateinit var optionSkipExisting: SwitchPreferenceView
        @BindView(R.id.option_restore_apk) lateinit var optionRestoreApk: SwitchPreferenceView
        @BindView(R.id.option_restore_data) lateinit var optionRestoreData: SwitchPreferenceView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Pair<Restore.Config, (Restore.Config) -> Unit>) {
            val config = item.first as AppRestoreConfig
            val callback = item.second
            optionSkipExisting.isChecked = config.skipExistingApps
            optionSkipExisting.setOnCheckedChangedListener { _, checked ->
                callback.invoke(config.copy(skipExistingApps = checked))
            }
            optionRestoreApk.isChecked = config.restoreApk
            optionRestoreApk.setOnCheckedChangedListener { _, checked ->
                callback.invoke(config.copy(restoreApk = checked))
            }
            optionRestoreData.isChecked = config.restoreData
            optionRestoreData.setOnCheckedChangedListener { _, checked ->
                callback.invoke(config.copy(restoreData = checked))
            }
        }

    }

    class FileConfigVH(parent: ViewGroup)
        : BaseVH(R.layout.task_editor_restore_configs_adapter_line_files, parent) {

        @BindView(R.id.option_replace_existing_files) lateinit var optionReplaceExisting: SwitchPreferenceView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Pair<Restore.Config, (Restore.Config) -> Unit>) {
            val config = item.first as FilesRestoreConfig
            val callback = item.second
            optionReplaceExisting.isChecked = config.replaceFiles
            optionReplaceExisting.setOnCheckedChangedListener { _, checked ->
                callback.invoke(config.copy(replaceFiles = checked))
            }
        }

    }
}