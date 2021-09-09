package eu.darken.bb.backup.ui.generator.list.actions

import android.view.ViewGroup
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.DataBinderModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.SimpleVHCreator
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.common.ui.ConfirmableActionAdapterVH
import javax.inject.Inject

//@PerChildFragment
class ActionsAdapter @Inject constructor() :
    ModularAdapter<ActionsAdapter.VH>(),
    DataAdapter<Confirmable<GeneratorsAction>> {

    override val data = mutableListOf<Confirmable<GeneratorsAction>>()

    init {
        modules.add(DataBinderModule<Confirmable<GeneratorsAction>, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup) : ConfirmableActionAdapterVH<GeneratorsAction>(parent) {
        override fun getIcon(item: GeneratorsAction): Int = item.iconRes

        override fun getLabel(item: GeneratorsAction): String = getString(item.labelRes)

    }
}