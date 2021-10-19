package eu.darken.bb.quickmode.ui.wizard.apps

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeWizardAppsOptionsItemBinding
import eu.darken.bb.quickmode.ui.wizard.common.WizardAdapter

class AppsOptionVH(parent: ViewGroup) :
    WizardAdapter.BaseVH<AppsOptionVH.Item, QuickmodeWizardAppsOptionsItemBinding>(
        R.layout.quickmode_wizard_apps_options_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeWizardAppsOptionsItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeWizardAppsOptionsItemBinding.(
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