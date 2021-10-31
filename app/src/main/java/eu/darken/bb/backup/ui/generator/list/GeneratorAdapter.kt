package eu.darken.bb.backup.ui.generator.list

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.differ.AsyncDiffer
import eu.darken.bb.common.lists.differ.HasAsyncDiffer
import eu.darken.bb.common.lists.differ.setupDiffer
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.databinding.GeneratorListAdapterLineBinding
import javax.inject.Inject


class GeneratorAdapter @Inject constructor() : ModularAdapter<GeneratorAdapter.VH>(),
    HasAsyncDiffer<GeneratorConfigOpt> {

    override val asyncDiffer: AsyncDiffer<*, GeneratorConfigOpt> = setupDiffer()

    override fun getItemCount(): Int = data.size

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it) })
    }

    class VH(parent: ViewGroup) : ModularAdapter.VH(R.layout.generator_list_adapter_line, parent),
        BindableVH<GeneratorConfigOpt, GeneratorListAdapterLineBinding> {

        override val viewBinding = lazy { GeneratorListAdapterLineBinding.bind(itemView) }
        override val onBindData: GeneratorListAdapterLineBinding.(
            item: GeneratorConfigOpt,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
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