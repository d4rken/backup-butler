package eu.darken.bb.tasks.ui.tasklist.actions

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.dagger.PerChildFragment
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataBinderModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.SimpleVHCreator
import javax.inject.Inject

@PerChildFragment
class ActionsAdapter @Inject constructor()
    : ModularAdapter<ActionsAdapter.VH>() {

    val data = mutableListOf<TaskActions>()

    init {
        modules.add(DataBinderModule<TaskActions, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.tasklist_action_adapter_line, parent), BindableVH<TaskActions> {
        @BindView(R.id.icon) lateinit var icon: ImageView
        @BindView(R.id.name) lateinit var label: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: TaskActions) {
            icon.setImageResource(item.iconRes)
            label.setText(item.labelRes)
        }

    }
}