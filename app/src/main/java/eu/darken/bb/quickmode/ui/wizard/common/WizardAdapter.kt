package eu.darken.bb.quickmode.ui.wizard.common

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.HasStableId
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.TypedVHCreatorMod

class WizardAdapter constructor(
    onProvideModules: (List<Item>) -> List<Module>
) :
    ModularAdapter<WizardAdapter.BaseVH<WizardAdapter.Item, ViewBinding>>(),
    DataAdapter<WizardAdapter.Item> {

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

    interface Item : HasStableId

}