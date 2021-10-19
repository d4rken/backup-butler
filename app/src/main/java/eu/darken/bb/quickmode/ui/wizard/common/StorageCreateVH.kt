package eu.darken.bb.quickmode.ui.wizard.common

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeWizardCommonStorageCreateItemBinding

class StorageCreateVH(parent: ViewGroup) :
    WizardAdapter.BaseVH<StorageCreateVH.Item, QuickmodeWizardCommonStorageCreateItemBinding>(
        R.layout.quickmode_wizard_common_storage_create_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeWizardCommonStorageCreateItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeWizardCommonStorageCreateItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        setupAction.setOnClickListener { item.onSetupStorage() }
    }

    data class Item(
        val onSetupStorage: () -> Unit,
    ) : StorageItem
}