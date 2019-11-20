package eu.darken.bb.task.ui.tasklist.actions

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
    : ModularAdapter<ActionsAdapter.VH>(), DataAdapter<Confirmable<TaskAction>> {

    override val data = mutableListOf<Confirmable<TaskAction>>()

    override fun getItemCount(): Int = data.size

    init {
        modules.add(DataBinderModule<Confirmable<TaskAction>, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    class VH(parent: ViewGroup) : ConfirmableActionAdapterVH<TaskAction>(parent) {
        override fun getIcon(item: TaskAction): Int = item.iconRes

        override fun getLabel(item: TaskAction): String = getString(item.labelRes)

    }
}