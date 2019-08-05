package eu.darken.bb.backups.ui.generator.list

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.backups.core.SpecGenerator
import eu.darken.bb.common.lists.*
import javax.inject.Inject


class GeneratorsAdapter @Inject constructor()
    : ModularAdapter<GeneratorsAdapter.VH>(), DataAdapter<SpecGenerator.Config> {

    override val data = mutableListOf<SpecGenerator.Config>()

    init {
        modules.add(DataBinderModule<SpecGenerator.Config, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.generator_list_adapter_line, parent), BindableVH<SpecGenerator.Config> {
        @BindView(R.id.type_icon) lateinit var typeIcon: ImageView
        @BindView(R.id.type_label) lateinit var typeLabel: TextView
        @BindView(R.id.label) lateinit var label: TextView
        @BindView(R.id.description) lateinit var description: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: SpecGenerator.Config) {
            typeIcon.setImageResource(item.generatorType.iconRes)
            typeLabel.setText(item.generatorType.labelRes)
            label.text = item.label
            description.text = item.getDescription(context)
//            statusIcon.setImageResource(R.drawable.ic_error_outline)
//            statusIcon.setColorFilter(getColor(R.color.colorError))
        }

    }
}