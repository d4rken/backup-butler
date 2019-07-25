package eu.darken.bb.storage.ui.list.actions

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

    val data = mutableListOf<StorageAction>()

    init {
        modules.add(DataBinderModule<StorageAction, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.tasklist_action_adapter_line, parent), BindableVH<StorageAction> {
        @BindView(R.id.icon) lateinit var icon: ImageView
        @BindView(R.id.name) lateinit var label: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: StorageAction) {
            icon.setImageResource(item.iconRes)
            label.setText(item.labelRes)
        }

    }
}