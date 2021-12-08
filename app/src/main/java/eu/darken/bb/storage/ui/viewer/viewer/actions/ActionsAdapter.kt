package eu.darken.bb.storage.ui.viewer.viewer.actions

import android.view.ViewGroup
import eu.darken.bb.common.lists.differ.AsyncDiffer
import eu.darken.bb.common.lists.differ.HasAsyncDiffer
import eu.darken.bb.common.lists.differ.setupDiffer
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.common.ui.ConfirmableActionAdapterVH
import javax.inject.Inject

class ActionsAdapter @Inject constructor() :
    ModularAdapter<ActionsAdapter.VH>(),
    HasAsyncDiffer<Confirmable<StorageViewerAction>> {

    override val asyncDiffer: AsyncDiffer<ActionsAdapter, Confirmable<StorageViewerAction>> = setupDiffer()

    override fun getItemCount(): Int = data.size

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it) })
    }

    class VH(parent: ViewGroup) : ConfirmableActionAdapterVH<StorageViewerAction>(parent) {
        override fun getIcon(item: StorageViewerAction): Int = item.iconRes

        override fun getLabel(item: StorageViewerAction): String = getString(item.labelRes)

    }
}