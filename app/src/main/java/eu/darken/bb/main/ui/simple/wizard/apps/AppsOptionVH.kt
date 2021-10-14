package eu.darken.bb.main.ui.simple.wizard.apps

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.SimpleModeWizardAppsOptionsItemBinding
import eu.darken.bb.main.ui.simple.wizard.common.WizardAdapter

class AppsOptionVH(parent: ViewGroup) :
    WizardAdapter.BaseVH<AppsOptionVH.Item, SimpleModeWizardAppsOptionsItemBinding>(
        R.layout.simple_mode_wizard_apps_options_item,
        parent
    ) {

    override val viewBinding = lazy {
        SimpleModeWizardAppsOptionsItemBinding.bind(itemView)
    }

    override val onBindData: SimpleModeWizardAppsOptionsItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    data class Item(
        val onToggleAutoInclude: (Boolean) -> Unit,
    ) : WizardAdapter.Item {
        override val stableId: Long = "CreateStorageVH".hashCode().toLong()
    }
}