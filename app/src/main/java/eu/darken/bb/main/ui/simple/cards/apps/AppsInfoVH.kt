package eu.darken.bb.main.ui.simple.cards.apps

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.SimpleModeMainAppsItemBinding
import eu.darken.bb.main.ui.simple.SimpleModeAdapter
import java.text.DateFormat
import java.time.Instant

class AppsInfoVH(parent: ViewGroup) : SimpleModeAdapter.BaseVH<AppsInfoVH.Item, SimpleModeMainAppsItemBinding>(
    R.layout.simple_mode_main_apps_item,
    parent
) {

    override val viewBinding = lazy {
        SimpleModeMainAppsItemBinding.bind(itemView)
    }

    private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    override val onBindData: SimpleModeMainAppsItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        primaryInfo.text = "// TODO"
        secondaryInfo.text = getString(
            R.string.general_last_backup_time_x,
            formatter.format(item.lastBackupAt)
        )
    }

    data class Item(
        val lastBackupAt: Instant,
        val onAppsBackupAction: () -> Unit,
        val onAppsViewAction: () -> Unit,
        val onAppsRestoreAction: () -> Unit,
        val onAppsEditAction: () -> Unit,
    ) : SimpleModeAdapter.Item {
        override val stableId: Long = LIST_ID
    }

    companion object {
        val LIST_ID = "Apps".hashCode().toLong()
    }
}