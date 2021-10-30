package eu.darken.bb.quickmode.ui.config.common

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeConfigCommonStorageErrorItemBinding

class StorageErrorMultipleVH(parent: ViewGroup) :
    ConfigAdapter.BaseVH<StorageErrorMultipleVH.Item, QuickmodeConfigCommonStorageErrorItemBinding>(
        R.layout.quickmode_config_common_storage_error_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeConfigCommonStorageErrorItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeConfigCommonStorageErrorItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    object Item : StorageItem
}