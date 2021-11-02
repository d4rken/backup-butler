package eu.darken.bb.quickmode.ui.files

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeMainFilesItemBinding
import eu.darken.bb.quickmode.core.QuickMode
import eu.darken.bb.quickmode.ui.QuickModeAdapter

class QuickFilesVH(parent: ViewGroup) :
    QuickModeAdapter.BaseVH<QuickFilesVH.Item, QuickmodeMainFilesItemBinding>(
        R.layout.quickmode_main_files_item,
        parent
    ) {

    override val viewBinding = lazy { QuickmodeMainFilesItemBinding.bind(itemView) }

    override val onBindData: QuickmodeMainFilesItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        primaryInfo.text = "// TODO"
        secondaryInfo.text = "// TODO"
        // TODO next back in XXhours

        val config = item.config
        viewAction.setOnClickListener { item.onView(config) }
        editAction.setOnClickListener { item.onEdit(config) }
        restoreAction.setOnClickListener { item.onRestore(config) }
        backupAction.setOnClickListener { item.onBackup(config) }
    }

    data class Item(
        val config: QuickMode.Config,
        val onBackup: (QuickMode.Config) -> Unit,
        val onView: (QuickMode.Config) -> Unit,
        val onRestore: (QuickMode.Config) -> Unit,
        val onEdit: (QuickMode.Config) -> Unit,
    ) : FilesItem
}