package eu.darken.bb.quickmode.ui.cards.apps

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeMainAppsItemBinding
import eu.darken.bb.quickmode.ui.QuickModeAdapter
import java.text.DateFormat
import java.time.Instant

class AppsInfoVH(parent: ViewGroup) : QuickModeAdapter.BaseVH<AppsInfoVH.Item, QuickmodeMainAppsItemBinding>(
    R.layout.quickmode_main_apps_item,
    parent
) {

    override val viewBinding = lazy {
        QuickmodeMainAppsItemBinding.bind(itemView)
    }

    private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    override val onBindData: QuickmodeMainAppsItemBinding.(
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
    ) : QuickModeAdapter.Item {
        override val stableId: Long = LIST_ID
    }

    companion object {
        val LIST_ID = "Apps".hashCode().toLong()
    }
}