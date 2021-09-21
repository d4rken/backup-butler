package eu.darken.bb.task.ui.editor.restore.config

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.lists.AsyncAutoDataAdapter
import eu.darken.bb.common.lists.AsyncDiffer
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.TypedVHCreatorMod
import eu.darken.bb.task.ui.editor.restore.config.app.AppConfigVH
import eu.darken.bb.task.ui.editor.restore.config.files.FileConfigVH
import javax.inject.Inject


class RestoreConfigAdapter @Inject constructor() : ModularAdapter<RestoreConfigAdapter.BaseVH<ViewBinding>>(),
    AsyncAutoDataAdapter<ConfigUIWrap> {

    override val asyncDiffer: AsyncDiffer<ConfigUIWrap> = AsyncDiffer(
        this,
        compareItem = { i1, i2 -> i1.stableId == i2.stableId },
        compareContent = { i1, i2 -> i1 == i2 }
    )

    override fun getItemId(position: Int): Long = data[position].stableId

    override fun getItemCount(): Int = data.size

    init {
        setHasStableIds(true)
        modules.add(DataBinderMod(data))
        modules.add(TypedVHCreatorMod({ data[it].config is AppRestoreConfig }) { AppConfigVH(it) })
        modules.add(TypedVHCreatorMod({ data[it].config is FilesRestoreConfig }) { FileConfigVH(it) })
    }

    abstract class BaseVH<V : ViewBinding>(@LayoutRes layoutRes: Int, parent: ViewGroup) :
        ModularAdapter.VH(layoutRes, parent),
        BindableVH<ConfigUIWrap, V>

}