package eu.darken.bb.backups.ui.generator.list.actions

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
    : ModularAdapter<ActionsAdapter.VH>(), DataAdapter<GeneratorsAction> {

    override val data = mutableListOf<GeneratorsAction>()

    init {
        modules.add(DataBinderModule<GeneratorsAction, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.task_list_action_adapter_line, parent), BindableVH<GeneratorsAction> {
        @BindView(R.id.icon) lateinit var icon: ImageView
        @BindView(R.id.name) lateinit var label: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: GeneratorsAction) {
            icon.setImageResource(item.iconRes)
            label.setText(item.labelRes)
        }

    }
}