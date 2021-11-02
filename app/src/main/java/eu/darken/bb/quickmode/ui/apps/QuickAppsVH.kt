package eu.darken.bb.quickmode.ui.apps

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeMainAppsItemBinding
import eu.darken.bb.quickmode.core.QuickMode
import eu.darken.bb.quickmode.ui.QuickModeAdapter

class QuickAppsVH(parent: ViewGroup) : QuickModeAdapter.BaseVH<QuickAppsVH.Item, QuickmodeMainAppsItemBinding>(
    R.layout.quickmode_main_apps_item,
    parent
) {

    override val viewBinding = lazy { QuickmodeMainAppsItemBinding.bind(itemView) }

    override val onBindData: QuickmodeMainAppsItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        primaryInfo.text = "// TODO"
        secondaryInfo.text = "// TODO"

        val config = item.config
        viewAction.setOnClickListener { item.onView(config) }
        editAction.setOnClickListener { item.onEdit(config) }
        backupAction.setOnClickListener { item.onBackup(config) }
        restoreAction.setOnClickListener { item.onRestore(config) }
    }

    data class Item(
        val config: QuickMode.Config,
        val onBackup: (QuickMode.Config) -> Unit,
        val onView: (QuickMode.Config) -> Unit,
        val onRestore: (QuickMode.Config) -> Unit,
        val onEdit: (QuickMode.Config) -> Unit,
    ) : QuickModeAdapter.Item {
        override val stableId: Long = LIST_ID
    }

    companion object {
        val LIST_ID = "Apps".hashCode().toLong()
    }
}