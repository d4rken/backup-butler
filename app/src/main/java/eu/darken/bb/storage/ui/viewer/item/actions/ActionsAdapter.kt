package eu.darken.bb.storage.ui.viewer.item.actions

import android.view.ViewGroup
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.common.ui.ConfirmableActionAdapterVH
import javax.inject.Inject

//@PerChildFragment
class ActionsAdapter @Inject constructor() :
    ModularAdapter<ActionsAdapter.VH>(),
    DataAdapter<Confirmable<ItemAction>> {

    override val data = mutableListOf<Confirmable<ItemAction>>()

    override fun getItemCount(): Int = data.size

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it) })
    }

    class VH(parent: ViewGroup) : ConfirmableActionAdapterVH<ItemAction>(parent) {
        override fun getIcon(item: ItemAction): Int = item.iconRes

        override fun getLabel(item: ItemAction): String = getString(item.labelRes)

    }
}