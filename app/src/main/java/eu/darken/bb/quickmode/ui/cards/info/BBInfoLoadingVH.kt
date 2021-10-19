package eu.darken.bb.quickmode.ui.cards.info

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeMainInfoLoadingItemBinding
import eu.darken.bb.quickmode.ui.QuickModeAdapter

class BBInfoLoadingVH(parent: ViewGroup) :
    QuickModeAdapter.BaseVH<BBInfoLoadingVH.Item, QuickmodeMainInfoLoadingItemBinding>(
        R.layout.quickmode_main_info_loading_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeMainInfoLoadingItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeMainInfoLoadingItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    object Item : QuickModeAdapter.Item {
        override val stableId: Long = BBInfoVH.LIST_ID
    }
}