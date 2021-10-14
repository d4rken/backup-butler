package eu.darken.bb.main.ui.simple.cards.info

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.SimpleModeMainInfoLoadingItemBinding
import eu.darken.bb.main.ui.simple.SimpleModeAdapter

class BBInfoLoadingVH(parent: ViewGroup) :
    SimpleModeAdapter.BaseVH<BBInfoLoadingVH.Item, SimpleModeMainInfoLoadingItemBinding>(
        R.layout.simple_mode_main_info_loading_item,
        parent
    ) {

    override val viewBinding = lazy {
        SimpleModeMainInfoLoadingItemBinding.bind(itemView)
    }

    override val onBindData: SimpleModeMainInfoLoadingItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    object Item : SimpleModeAdapter.Item {
        override val stableId: Long = BBInfoVH.LIST_ID
    }
}