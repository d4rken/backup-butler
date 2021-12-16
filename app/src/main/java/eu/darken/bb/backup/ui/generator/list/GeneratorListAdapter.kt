package eu.darken.bb.backup.ui.generator.list

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.differ.AsyncDiffer
import eu.darken.bb.common.lists.differ.DifferItem
import eu.darken.bb.common.lists.differ.HasAsyncDiffer
import eu.darken.bb.common.lists.differ.setupDiffer
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.databinding.GeneratorListAdapterLineBinding
import javax.inject.Inject


class GeneratorListAdapter @Inject constructor() : ModularAdapter<GeneratorListAdapter.VH>(),
    HasAsyncDiffer<GeneratorListAdapter.Item> {

    override val asyncDiffer: AsyncDiffer<*, Item> = setupDiffer()

    override fun getItemCount(): Int = data.size

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it) })
    }

    data class Item(
        val configOpt: GeneratorConfigOpt,
        val onClick: (Generator.Id) -> Unit,
    ) : DifferItem {
        override val stableId: Long
            get() = configOpt.hashCode().toLong()
    }

    class VH(parent: ViewGroup) : ModularAdapter.VH(R.layout.generator_list_adapter_line, parent),
        BindableVH<Item, GeneratorListAdapterLineBinding> {

        override val viewBinding = lazy { GeneratorListAdapterLineBinding.bind(itemView) }
        override val onBindData: GeneratorListAdapterLineBinding.(
            item: Item,
            payloads: List<Any>
        ) -> Unit = { item, _ ->

            itemView.setOnClickListener { item.onClick(item.configOpt.generatorId) }

            if (item.configOpt.config != null) {
                val config = item.configOpt.config

                val typeSb = StringBuilder(getString(config.generatorType.labelRes))
                if (config.isSingleUse) {
                    typeSb.append(" (${getString(R.string.singleuse_item_label)})")
                }
                typeLabel.text = typeSb.toString()

                label.text = config.label
                description.text = config.getDescription(context)

                typeIcon.setColorFilter(context.getColorForAttr(android.R.attr.textColorSecondary))
                typeIcon.setImageResource(config.generatorType.iconRes)
            } else {
                typeLabel.setText(R.string.general_unknown_label)
                label.text = "?"
                description.text = getString(R.string.general_error_cant_access_msg, item.configOpt.generatorId)

                typeIcon.setColorFilter(getColor(R.color.colorError))
                typeIcon.setImageResource(R.drawable.ic_error_outline)
            }
        }
    }
}