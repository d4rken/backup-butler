package eu.darken.bb.quickmode.ui.common

import android.view.ViewGroup
import eu.darken.bb.BackupButler
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeMainInfoItemBinding
import eu.darken.bb.quickmode.ui.QuickModeAdapter

class QuickBackupVH(parent: ViewGroup) : QuickModeAdapter.BaseVH<QuickBackupVH.Item, QuickmodeMainInfoItemBinding>(
    R.layout.quickmode_main_info_item,
    parent
) {

    override val viewBinding = lazy {
        QuickmodeMainInfoItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeMainInfoItemBinding.(
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
    ) : QuickModeAdapter.Item {
        override val stableId: Long = LIST_ID
    }

    companion object {
        val LIST_ID = "Info".hashCode().toLong()
    }
}