package eu.darken.bb.main.ui.simple

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.HasStableId
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.TypedVHCreatorMod
import eu.darken.bb.main.ui.simple.cards.apps.AppsInfoCreateVH
import eu.darken.bb.main.ui.simple.cards.apps.AppsInfoLoadingVH
import eu.darken.bb.main.ui.simple.cards.apps.AppsInfoVH
import eu.darken.bb.main.ui.simple.cards.files.FilesInfoCreateVH
import eu.darken.bb.main.ui.simple.cards.files.FilesInfoLoadingVH
import eu.darken.bb.main.ui.simple.cards.files.FilesInfoVH
import eu.darken.bb.main.ui.simple.cards.hints.AdvancedModeHintsVH
import eu.darken.bb.main.ui.simple.cards.info.BBInfoLoadingVH
import eu.darken.bb.main.ui.simple.cards.info.BBInfoVH
import javax.inject.Inject

class SimpleModeAdapter @Inject constructor() :
    ModularAdapter<SimpleModeAdapter.BaseVH<SimpleModeAdapter.Item, ViewBinding>>(),
    DataAdapter<SimpleModeAdapter.Item> {

    override val data = mutableListOf<Item>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(TypedVHCreatorMod({ data[it] is AdvancedModeHintsVH.Item }) { AdvancedModeHintsVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is BBInfoVH.Item }) { BBInfoVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is BBInfoLoadingVH.Item }) { BBInfoLoadingVH(it) })
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