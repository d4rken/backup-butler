package eu.darken.bb.main.ui.simple.wizard.common

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.SimpleModeWizardCommonAutoSetupItemBinding

class AutoSetupVH(parent: ViewGroup) :
    WizardAdapter.BaseVH<AutoSetupVH.Item, SimpleModeWizardCommonAutoSetupItemBinding>(
        R.layout.simple_mode_wizard_common_auto_setup_item,
        parent
    ) {

    override val viewBinding = lazy {
        SimpleModeWizardCommonAutoSetupItemBinding.bind(itemView)
    }

    override val onBindData: SimpleModeWizardCommonAutoSetupItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        setupAction.setOnClickListener { item.onAutoSetup() }
    }

    data class Item(
        val onAutoSetup: () -> Unit
    ) : WizardAdapter.Item {
        override val stableId: Long = Item::class.hashCode().toLong()
    }
}