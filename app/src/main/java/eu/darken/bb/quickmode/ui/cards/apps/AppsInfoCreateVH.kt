package eu.darken.bb.quickmode.ui.cards.apps

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeMainAppsCreateItemBinding
import eu.darken.bb.quickmode.ui.QuickModeAdapter

class AppsInfoCreateVH(parent: ViewGroup) :
    QuickModeAdapter.BaseVH<AppsInfoCreateVH.Item, QuickmodeMainAppsCreateItemBinding>(
        R.layout.quickmode_main_apps_create_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeMainAppsCreateItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeMainAppsCreateItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        createAction.setOnClickListener { item.onCreateAppsTaskAction() }
    }

    data class Item(
        val onCreateAppsTaskAction: () -> Unit,
    ) : QuickModeAdapter.Item {
        override val stableId: Long = AppsInfoVH.LIST_ID
    }
}