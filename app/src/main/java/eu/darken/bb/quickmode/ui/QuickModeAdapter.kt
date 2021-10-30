package eu.darken.bb.quickmode.ui

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.HasStableId
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.TypedVHCreatorMod
import eu.darken.bb.quickmode.ui.apps.AppsInfoCreateVH
import eu.darken.bb.quickmode.ui.apps.AppsInfoLoadingVH
import eu.darken.bb.quickmode.ui.apps.AppsInfoVH
import eu.darken.bb.quickmode.ui.common.AdvancedModeHintsVH
import eu.darken.bb.quickmode.ui.files.FilesInfoCreateVH
import eu.darken.bb.quickmode.ui.files.FilesInfoLoadingVH
import eu.darken.bb.quickmode.ui.files.FilesInfoVH
import javax.inject.Inject

class QuickModeAdapter @Inject constructor() :
    ModularAdapter<QuickModeAdapter.BaseVH<QuickModeAdapter.Item, ViewBinding>>(),
    DataAdapter<QuickModeAdapter.Item> {

    override val data = mutableListOf<Item>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(TypedVHCreatorMod({ data[it] is AdvancedModeHintsVH.Item }) { AdvancedModeHintsVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is AppsInfoVH.Item }) { AppsInfoVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is AppsInfoLoadingVH.Item }) { AppsInfoLoadingVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is AppsInfoCreateVH.Item }) { AppsInfoCreateVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is FilesInfoVH.Item }) { FilesInfoVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is FilesInfoLoadingVH.Item }) { FilesInfoLoadingVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is FilesInfoCreateVH.Item }) { FilesInfoCreateVH(it) })
    }

    override fun getItemCount(): Int = data.size

    abstract class BaseVH<D : Item, B : ViewBinding>(
        @LayoutRes layoutId: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutId, parent), BindableVH<D, B>

    interface Item : HasStableId

}