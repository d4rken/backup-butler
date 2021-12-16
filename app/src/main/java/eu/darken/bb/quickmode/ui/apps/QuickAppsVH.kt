package eu.darken.bb.quickmode.ui.apps

import android.text.format.Formatter
import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeMainAppsItemBinding
import eu.darken.bb.quickmode.core.QuickMode
import eu.darken.bb.quickmode.ui.QuickModeAdapter
import eu.darken.bb.storage.core.Storage

class QuickAppsVH(parent: ViewGroup) : QuickModeAdapter.BaseVH<QuickAppsVH.Item, QuickmodeMainAppsItemBinding>(
    R.layout.quickmode_main_apps_item,
    parent
) {

    override val viewBinding = lazy { QuickmodeMainAppsItemBinding.bind(itemView) }

    override val onBindData: QuickmodeMainAppsItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

        primaryInfo.text = when {
            item.storageInfos.none { it.info?.config != null } -> getString(R.string.progress_loading_label)
            else -> {
                val config = item.storageInfos.firstOrNull { it.info?.config != null }?.info?.config
                val label = config?.label
                val type = config?.storageType?.labelRes?.let { getString(it) }
                "$label (${type})"
            }
        }

        secondaryInfo.text = when {
            item.storageInfos.all { !it.isFinished } -> getString(R.string.progress_loading_label)
            else -> {
                // TODO Support infos for more than 1 storage
                item.storageInfos.firstOrNull()?.info?.status?.let {
                    val countStr = getQuantityString(R.plurals.x_items, it.itemCount)
                    val sizeStr = Formatter.formatShortFileSize(context, it.totalSize)
                    "$countStr, $sizeStr"
                }
            }
        }

        val config = item.config
        viewAction.setOnClickListener { item.onView(config) }
        editAction.setOnClickListener { item.onEdit(config) }
        backupAction.setOnClickListener { item.onBackup(config) }
        restoreAction.setOnClickListener { item.onRestore(config) }
    }

    data class Item(
        val config: QuickMode.Config,
        val storageInfos: Collection<Storage.InfoOpt>,
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