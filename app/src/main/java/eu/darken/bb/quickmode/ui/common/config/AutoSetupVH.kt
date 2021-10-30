package eu.darken.bb.quickmode.ui.common.config

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeConfigCommonAutoSetupItemBinding

class AutoSetupVH(parent: ViewGroup) :
    ConfigAdapter.BaseVH<AutoSetupVH.Item, QuickmodeConfigCommonAutoSetupItemBinding>(
        R.layout.quickmode_config_common_auto_setup_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeConfigCommonAutoSetupItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeConfigCommonAutoSetupItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        setupAction.setOnClickListener { item.onAutoSetup() }
    }

    data class Item(
        val onAutoSetup: () -> Unit
    ) : ConfigAdapter.Item {
        override val stableId: Long = Item::class.hashCode().toLong()
    }
}