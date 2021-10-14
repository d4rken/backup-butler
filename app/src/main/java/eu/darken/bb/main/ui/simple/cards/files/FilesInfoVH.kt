package eu.darken.bb.main.ui.simple.cards.files

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.SimpleModeMainFilesItemBinding
import eu.darken.bb.main.ui.simple.SimpleModeAdapter
import java.text.DateFormat
import java.time.Instant

class FilesInfoVH(parent: ViewGroup) :
    SimpleModeAdapter.BaseVH<FilesInfoVH.Item, SimpleModeMainFilesItemBinding>(
        R.layout.simple_mode_main_files_item,
        parent
    ) {

    override val viewBinding = lazy {
        SimpleModeMainFilesItemBinding.bind(itemView)
    }

    private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    override val onBindData: SimpleModeMainFilesItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        primaryInfo.text = "// TODO"
        secondaryInfo.text = getString(
            R.string.general_last_backup_time_x,
            formatter.format(item.lastBackupAt)
        )
        // TODO next back in XXhours
    }

    data class Item(
        val lastBackupAt: Instant,
        val onFilesBackupAction: () -> Unit,
        val onFilesViewAction: () -> Unit,
        val onFilesRestoreAction: () -> Unit,
        val onFilesEditAction: () -> Unit,
    ) : SimpleModeAdapter.Item {
        override val stableId: Long = LIST_ID
    }

    companion object {
        val LIST_ID = "Files".hashCode().toLong()
    }
}