package eu.darken.bb.task.ui.editor.restore.config

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.differ.AsyncDiffer
import eu.darken.bb.common.lists.differ.HasAsyncDiffer
import eu.darken.bb.common.lists.differ.setupDiffer
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.TypedVHCreatorMod
import eu.darken.bb.task.ui.editor.restore.config.app.AppConfigVH
import eu.darken.bb.task.ui.editor.restore.config.files.FileConfigVH
import javax.inject.Inject


class RestoreConfigAdapter @Inject constructor() : ModularAdapter<RestoreConfigAdapter.BaseVH<ViewBinding>>(),
    HasAsyncDiffer<ConfigUIWrap> {

    override val asyncDiffer: AsyncDiffer<RestoreConfigAdapter, ConfigUIWrap> = setupDiffer()

    override fun getItemCount(): Int = data.size

    init {
        modules.add(DataBinderMod(data))
        modules.add(TypedVHCreatorMod({ data[it].config is AppRestoreConfig }) { AppConfigVH(it) })
        modules.add(TypedVHCreatorMod({ data[it].config is FilesRestoreConfig }) { FileConfigVH(it) })
    }

    abstract class BaseVH<V : ViewBinding>(@LayoutRes layoutRes: Int, parent: ViewGroup) :
        ModularAdapter.VH(layoutRes, parent),
        BindableVH<ConfigUIWrap, V>

}