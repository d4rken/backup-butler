package eu.darken.bb.quickmode.ui.common.config

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.differ.DifferItem
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.TypedVHCreatorMod

class ConfigAdapter constructor(
    onProvideModules: (List<Item>) -> List<Module>
) :
    ModularAdapter<ConfigAdapter.BaseVH<ConfigAdapter.Item, ViewBinding>>(),
    DataAdapter<ConfigAdapter.Item> {

    override val data = mutableListOf<Item>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(TypedVHCreatorMod({ data[it] is StorageCreateVH.Item }) { StorageCreateVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is StorageInfoVH.Item }) { StorageInfoVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is StorageErrorMultipleVH.Item }) { StorageErrorMultipleVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is AutoSetupVH.Item }) { AutoSetupVH(it) })
        modules.addAll(onProvideModules(data))
    }

    override fun getItemCount(): Int = data.size

    abstract class BaseVH<D : Item, B : ViewBinding>(
        @LayoutRes layoutId: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutId, parent), BindableVH<D, B>

    interface Item : DifferItem

}