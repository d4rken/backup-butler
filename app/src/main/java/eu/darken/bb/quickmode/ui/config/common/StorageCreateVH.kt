package eu.darken.bb.quickmode.ui.config.common

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeConfigCommonAutoSetupItemBinding

class StorageCreateVH(parent: ViewGroup) :
    ConfigAdapter.BaseVH<StorageCreateVH.Item, QuickmodeConfigCommonAutoSetupItemBinding>(
        R.layout.quickmode_config_common_storage_create_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeConfigCommonAutoSetupItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeConfigCommonAutoSetupItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        setupAction.setOnClickListener { item.onSetupStorage() }
    }

    data class Item(
        val onSetupStorage: () -> Unit,
    ) : StorageItem
}