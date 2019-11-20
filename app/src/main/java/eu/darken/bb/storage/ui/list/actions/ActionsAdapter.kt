package eu.darken.bb.storage.ui.list.actions

import android.view.ViewGroup
import eu.darken.bb.common.dagger.PerChildFragment
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.DataBinderModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.SimpleVHCreator
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.common.ui.ConfirmableActionAdapterVH
import javax.inject.Inject

@PerChildFragment
class ActionsAdapter @Inject constructor()
    : ModularAdapter<ActionsAdapter.VH>(), DataAdapter<Confirmable<StorageAction>> {

    override val data = mutableListOf<Confirmable<StorageAction>>()

    override fun getItemCount(): Int = data.size

    init {
        modules.add(DataBinderModule<Confirmable<StorageAction>, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    class VH(parent: ViewGroup) : ConfirmableActionAdapterVH<StorageAction>(parent) {

        override fun getIcon(item: StorageAction): Int = item.iconRes

        override fun getLabel(item: StorageAction): String = getString(item.labelRes)

    }
}