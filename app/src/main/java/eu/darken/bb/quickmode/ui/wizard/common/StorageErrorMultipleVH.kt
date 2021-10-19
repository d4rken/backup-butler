package eu.darken.bb.quickmode.ui.wizard.common

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeWizardCommonStorageErrorItemBinding

class StorageErrorMultipleVH(parent: ViewGroup) :
    WizardAdapter.BaseVH<StorageErrorMultipleVH.Item, QuickmodeWizardCommonStorageErrorItemBinding>(
        R.layout.quickmode_wizard_common_storage_error_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeWizardCommonStorageErrorItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeWizardCommonStorageErrorItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    object Item : StorageItem
}