package eu.darken.bb.storage.ui.viewer.content.actions

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.dagger.PerChildFragment
import eu.darken.bb.common.lists.*
import javax.inject.Inject

@PerChildFragment
class ActionsAdapter @Inject constructor()
    : ModularAdapter<ActionsAdapter.VH>(), DataAdapter<ContentAction> {

    override val data = mutableListOf<ContentAction>()

    init {
        modules.add(DataBinderModule<ContentAction, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.task_list_action_adapter_line, parent), BindableVH<ContentAction> {
        @BindView(R.id.icon) lateinit var icon: ImageView
        @BindView(R.id.name) lateinit var label: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: ContentAction) {
            icon.setImageResource(item.iconRes)
            label.setText(item.labelRes)
        }

    }
}