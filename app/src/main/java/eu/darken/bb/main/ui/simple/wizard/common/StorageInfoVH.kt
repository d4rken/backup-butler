package eu.darken.bb.main.ui.simple.wizard.common

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.SimpleModeWizardCommonStorageInfoItemBinding
import eu.darken.bb.storage.core.Storage

class StorageInfoVH(parent: ViewGroup) :
    WizardAdapter.BaseVH<StorageInfoVH.Item, SimpleModeWizardCommonStorageInfoItemBinding>(
        R.layout.simple_mode_wizard_common_storage_info_item,
        parent
    ) {

    override val viewBinding = lazy {
        SimpleModeWizardCommonStorageInfoItemBinding.bind(itemView)
    }

    override val onBindData: SimpleModeWizardCommonStorageInfoItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    data class Item(
        val info: Storage.InfoOpt,
        val onView: () -> Unit,
        val onDetach: () -> Unit,
        val onDelete: () -> Unit,
    ) : StorageItem
}