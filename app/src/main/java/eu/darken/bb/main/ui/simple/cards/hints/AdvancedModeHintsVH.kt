package eu.darken.bb.main.ui.simple.cards.hints

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.SimpleModeMainHintUimodesItemBinding
import eu.darken.bb.main.ui.simple.SimpleModeAdapter

class AdvancedModeHintsVH(parent: ViewGroup) :
    SimpleModeAdapter.BaseVH<AdvancedModeHintsVH.Item, SimpleModeMainHintUimodesItemBinding>(
        R.layout.simple_mode_main_hint_uimodes_item,
        parent
    ) {

    override val viewBinding = lazy {
        SimpleModeMainHintUimodesItemBinding.bind(itemView)
    }

    override val onBindData: SimpleModeMainHintUimodesItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        dismissAction.setOnClickListener { item.onDismiss() }
    }

    data class Item(
        val onDismiss: () -> Unit
    ) : SimpleModeAdapter.Item {
        override val stableId: Long = this.javaClass.hashCode().toLong()
    }
}