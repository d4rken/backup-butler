package eu.darken.bb.main.ui.simple.wizard.common

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.SimpleModeWizardCommonStorageErrorItemBinding

class StorageErrorMultipleVH(parent: ViewGroup) :
    WizardAdapter.BaseVH<StorageErrorMultipleVH.Item, SimpleModeWizardCommonStorageErrorItemBinding>(
        R.layout.simple_mode_wizard_common_storage_error_item,
        parent
    ) {

    override val viewBinding = lazy {
        SimpleModeWizardCommonStorageErrorItemBinding.bind(itemView)
    }

    override val onBindData: SimpleModeWizardCommonStorageErrorItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    object Item : StorageItem
}