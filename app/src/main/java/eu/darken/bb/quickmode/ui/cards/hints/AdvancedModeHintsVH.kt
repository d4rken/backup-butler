package eu.darken.bb.quickmode.ui.cards.hints

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeMainHintUimodesItemBinding
import eu.darken.bb.quickmode.ui.QuickModeAdapter

class AdvancedModeHintsVH(parent: ViewGroup) :
    QuickModeAdapter.BaseVH<AdvancedModeHintsVH.Item, QuickmodeMainHintUimodesItemBinding>(
        R.layout.quickmode_main_hint_uimodes_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeMainHintUimodesItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeMainHintUimodesItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        dismissAction.setOnClickListener { item.onDismiss() }
    }

    data class Item(
        val onDismiss: () -> Unit
    ) : QuickModeAdapter.Item {
        override val stableId: Long = this.javaClass.hashCode().toLong()
    }
}