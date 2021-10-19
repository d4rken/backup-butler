package eu.darken.bb.quickmode.ui.wizard.common

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeWizardCommonAutoSetupItemBinding

class AutoSetupVH(parent: ViewGroup) :
    WizardAdapter.BaseVH<AutoSetupVH.Item, QuickmodeWizardCommonAutoSetupItemBinding>(
        R.layout.quickmode_wizard_common_auto_setup_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeWizardCommonAutoSetupItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeWizardCommonAutoSetupItemBinding.(
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