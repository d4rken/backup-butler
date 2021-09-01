package eu.darken.bb.backup.ui.generator.list

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.lists.*
import javax.inject.Inject


class GeneratorAdapter @Inject constructor() : ModularAdapter<GeneratorAdapter.VH>(), DataAdapter<GeneratorConfigOpt> {

    override val data = mutableListOf<GeneratorConfigOpt>()

    init {
        modules.add(DataBinderModule<GeneratorConfigOpt, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup) : ModularAdapter.VH(R.layout.generator_list_adapter_line, parent),
        BindableVH<GeneratorConfigOpt> {
        @BindView(R.id.type_icon) lateinit var typeIcon: ImageView
        @BindView(R.id.type_label) lateinit var typeLabel: TextView
        @BindView(R.id.label) lateinit var label: TextView
        @BindView(R.id.description) lateinit var description: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: GeneratorConfigOpt) {
            if (item.config != null) {
                val config = item.config

                typeLabel.setText(config.generatorType.labelRes)
                label.text = config.label
                description.text = config.getDescription(context)

                typeIcon.setColorFilter(context.getColorForAttr(android.R.attr.textColorSecondary))
                typeIcon.setImageResource(config.generatorType.iconRes)
            } else {
                typeLabel.setText(R.string.general_unknown_label)
                label.text = "?"
                description.text = getString(R.string.general_error_cant_access_msg, item.generatorId)

                typeIcon.setColorFilter(getColor(R.color.colorError))
                typeIcon.setImageResource(R.drawable.ic_error_outline)
            }
        }

    }
}