package eu.darken.bb.quickmode.ui.config.apps

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeAppsConfigOptionsItemBinding
import eu.darken.bb.quickmode.ui.config.common.ConfigAdapter

class AppsOptionVH(parent: ViewGroup) :
    ConfigAdapter.BaseVH<AppsOptionVH.Item, QuickmodeAppsConfigOptionsItemBinding>(
        R.layout.quickmode_apps_config_options_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeAppsConfigOptionsItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeAppsConfigOptionsItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    data class Item(
        val onToggleAutoInclude: (Boolean) -> Unit,
    ) : ConfigAdapter.Item {
        override val stableId: Long = "CreateStorageVH".hashCode().toLong()
    }
}