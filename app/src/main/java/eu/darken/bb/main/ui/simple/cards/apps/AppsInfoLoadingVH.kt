package eu.darken.bb.main.ui.simple.cards.apps

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.SimpleModeMainAppsLoadingItemBinding
import eu.darken.bb.main.ui.simple.SimpleModeAdapter

class AppsInfoLoadingVH(parent: ViewGroup) :
    SimpleModeAdapter.BaseVH<AppsInfoLoadingVH.Item, SimpleModeMainAppsLoadingItemBinding>(
        R.layout.simple_mode_main_apps_loading_item,
        parent
    ) {

    override val viewBinding = lazy {
        SimpleModeMainAppsLoadingItemBinding.bind(itemView)
    }

    override val onBindData: SimpleModeMainAppsLoadingItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    object Item : SimpleModeAdapter.Item {
        override val stableId: Long = AppsInfoVH.LIST_ID
    }
}