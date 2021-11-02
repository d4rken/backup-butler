package eu.darken.bb.quickmode.ui

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.differ.DifferItem
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.TypedVHCreatorMod
import eu.darken.bb.quickmode.ui.apps.QuickAppsCreateVH
import eu.darken.bb.quickmode.ui.apps.QuickAppsLoadingVH
import eu.darken.bb.quickmode.ui.apps.QuickAppsVH
import eu.darken.bb.quickmode.ui.common.AdvancedModeHintsVH
import eu.darken.bb.quickmode.ui.files.QuickFilesCreateVH
import eu.darken.bb.quickmode.ui.files.QuickFilesLoadingVH
import eu.darken.bb.quickmode.ui.files.QuickFilesVH
import javax.inject.Inject

class QuickModeAdapter @Inject constructor() :
    ModularAdapter<QuickModeAdapter.BaseVH<QuickModeAdapter.Item, ViewBinding>>(),
    DataAdapter<QuickModeAdapter.Item> {

    override val data = mutableListOf<Item>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(TypedVHCreatorMod({ data[it] is AdvancedModeHintsVH.Item }) { AdvancedModeHintsVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is QuickAppsVH.Item }) { QuickAppsVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is QuickAppsLoadingVH.Item }) { QuickAppsLoadingVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is QuickAppsCreateVH.Item }) { QuickAppsCreateVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is QuickFilesVH.Item }) { QuickFilesVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is QuickFilesLoadingVH.Item }) { QuickFilesLoadingVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is QuickFilesCreateVH.Item }) { QuickFilesCreateVH(it) })
    }

    override fun getItemCount(): Int = data.size

    abstract class BaseVH<D : Item, B : ViewBinding>(
        @LayoutRes layoutId: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutId, parent), BindableVH<D, B>

    interface Item : DifferItem

}