package eu.darken.bb.quickmode.ui.files

import android.app.AlertDialog
import android.text.format.Formatter
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeMainFilesItemBinding
import eu.darken.bb.quickmode.core.QuickMode
import eu.darken.bb.quickmode.ui.QuickModeAdapter
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.results.TaskResult

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

        viewErrorAction.isGone = item.lastTaskResult?.state != TaskResult.State.ERROR

        icon.isInvisible = !item.storageInfos.all { it.isFinished }
        loadingAnimation.isGone = item.storageInfos.all { it.isFinished }

        val config = item.config
        viewErrorAction.setOnClickListener {
            viewErrorAction.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle(R.string.general_last_result_label)
                    .setMessage("${item.lastTaskResult!!.primary}\n${item.lastTaskResult.secondary}")
                    .setPositiveButton(R.string.general_ok_action) { _, _ -> }
                    .show()
            }
        }
        editAction.setOnClickListener { item.onEdit(config) }
        restoreAction.setOnClickListener { item.onRestore(config) }
        backupAction.setOnClickListener { item.onBackup(config) }
    }

    data class Item(
        val config: QuickMode.Config,
        val storageInfos: Collection<Storage.InfoOpt>,
        val lastTaskResult: TaskResult? = null,
        val onEdit: (QuickMode.Config) -> Unit,
        val onBackup: (QuickMode.Config) -> Unit,
        val onRestore: (QuickMode.Config) -> Unit,
    ) : FilesItem
}