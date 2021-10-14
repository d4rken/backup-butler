package eu.darken.bb.main.ui.simple.wizard.common

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.SimpleModeWizardCommonStorageCreateItemBinding

class StorageCreateVH(parent: ViewGroup) :
    WizardAdapter.BaseVH<StorageCreateVH.Item, SimpleModeWizardCommonStorageCreateItemBinding>(
        R.layout.simple_mode_wizard_common_storage_create_item,
        parent
    ) {

    override val viewBinding = lazy {
        SimpleModeWizardCommonStorageCreateItemBinding.bind(itemView)
    }

    override val onBindData: SimpleModeWizardCommonStorageCreateItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        setupAction.setOnClickListener { item.onSetupStorage() }
    }

    data class Item(
        val onSetupStorage: () -> Unit,
    ) : StorageItem
}