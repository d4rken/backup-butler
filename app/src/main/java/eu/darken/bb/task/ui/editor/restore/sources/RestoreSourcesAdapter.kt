package eu.darken.bb.task.ui.editor.restore.sources

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.lists.*
import eu.darken.bb.common.ui.SwitchPreferenceView
import eu.darken.bb.storage.ui.list.StorageInfoOpt
import javax.inject.Inject


class RestoreSourcesAdapter @Inject constructor()
    : ModularAdapter<RestoreSourcesAdapter.BaseVH>(), DataAdapter<Any> {

    override val data = mutableListOf<Any>()

    init {
        modules.add(DataBinderModule<Any, BaseVH>(data))
        modules.add(TypedVHCreator(0, { data[it] is StorageInfoOpt }) { StorageVH(it) })
        modules.add(TypedVHCreator(1, { data[it] is BackupSpec.Id }) { SpecVH(it) })
        modules.add(TypedVHCreator(2, { data[it] is Backup.Id }) { BackupVH(it) })
    }

    override fun getItemCount(): Int = data.size


    abstract class BaseVH(@LayoutRes layoutRes: Int, parent: ViewGroup)
        : ModularAdapter.VH(layoutRes, parent), BindableVH<Any>

    class StorageVH(parent: ViewGroup)
        : BaseVH(R.layout.task_editor_restore_sources_adapter_line_storage, parent) {


        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Any) {
            item as StorageInfoOpt
        }

    }

    class SpecVH(parent: ViewGroup)
        : BaseVH(R.layout.task_editor_restore_configs_adapter_line_files, parent) {

        @BindView(R.id.option_replace_existing_files)
        lateinit var optionReplaceExisting: SwitchPreferenceView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Any) {

        }

    }

    class BackupVH(parent: ViewGroup)
        : BaseVH(R.layout.task_editor_restore_configs_adapter_line_files, parent) {

        @BindView(R.id.option_replace_existing_files)
        lateinit var optionReplaceExisting: SwitchPreferenceView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Any) {

        }

    }
}