package eu.darken.bb.main.ui.simple.cards.quick

import android.view.ViewGroup
import eu.darken.bb.BackupButler
import eu.darken.bb.R
import eu.darken.bb.databinding.SimpleModeMainInfoItemBinding
import eu.darken.bb.main.ui.simple.SimpleModeAdapter

class QuickBackupVH(parent: ViewGroup) : SimpleModeAdapter.BaseVH<QuickBackupVH.Item, SimpleModeMainInfoItemBinding>(
    R.layout.simple_mode_main_info_item,
    parent
) {

    override val viewBinding = lazy {
        SimpleModeMainInfoItemBinding.bind(itemView)
    }

    override val onBindData: SimpleModeMainInfoItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        primaryInfo.text = item.appInfo.fullVersionString
        secondaryInfo.text = "// TODO root status"
        tertiaryInfo.text = "// TODO pro status"
    }

    data class Item(
        val appInfo: BackupButler.AppInfo,
        val onUpgradeAction: () -> Unit
    ) : SimpleModeAdapter.Item {
        override val stableId: Long = LIST_ID
    }

    companion object {
        val LIST_ID = "Info".hashCode().toLong()
    }
}