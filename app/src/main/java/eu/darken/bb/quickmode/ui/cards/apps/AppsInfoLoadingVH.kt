package eu.darken.bb.quickmode.ui.cards.apps

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeMainAppsLoadingItemBinding
import eu.darken.bb.quickmode.ui.QuickModeAdapter

class AppsInfoLoadingVH(parent: ViewGroup) :
    QuickModeAdapter.BaseVH<AppsInfoLoadingVH.Item, QuickmodeMainAppsLoadingItemBinding>(
        R.layout.quickmode_main_apps_loading_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeMainAppsLoadingItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeMainAppsLoadingItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    object Item : QuickModeAdapter.Item {
        override val stableId: Long = AppsInfoVH.LIST_ID
    }
}