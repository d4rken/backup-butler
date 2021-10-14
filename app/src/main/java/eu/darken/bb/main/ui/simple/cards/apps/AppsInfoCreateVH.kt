package eu.darken.bb.main.ui.simple.cards.apps

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.SimpleModeMainAppsCreateItemBinding
import eu.darken.bb.main.ui.simple.SimpleModeAdapter

class AppsInfoCreateVH(parent: ViewGroup) :
    SimpleModeAdapter.BaseVH<AppsInfoCreateVH.Item, SimpleModeMainAppsCreateItemBinding>(
        R.layout.simple_mode_main_apps_create_item,
        parent
    ) {

    override val viewBinding = lazy {
        SimpleModeMainAppsCreateItemBinding.bind(itemView)
    }

    override val onBindData: SimpleModeMainAppsCreateItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        createAction.setOnClickListener { item.onCreateAppsTaskAction() }
    }

    data class Item(
        val onCreateAppsTaskAction: () -> Unit,
    ) : SimpleModeAdapter.Item {
        override val stableId: Long = AppsInfoVH.LIST_ID
    }
}