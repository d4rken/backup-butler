package eu.darken.bb.quickmode.ui.files

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeMainFilesItemBinding
import eu.darken.bb.quickmode.ui.QuickModeAdapter
import eu.darken.bb.task.core.Task

class FilesInfoVH(parent: ViewGroup) :
    QuickModeAdapter.BaseVH<FilesInfoVH.Item, QuickmodeMainFilesItemBinding>(
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

        val taskId = item.task.taskId
        viewAction.setOnClickListener { item.onView(taskId) }
        editAction.setOnClickListener { item.onEdit(taskId) }
        restoreAction.setOnClickListener { item.onRestore(taskId) }
        backupAction.setOnClickListener { item.onBackup(taskId) }
    }

    data class Item(
        val task: Task,
        val onBackup: (Task.Id) -> Unit,
        val onView: (Task.Id) -> Unit,
        val onRestore: (Task.Id) -> Unit,
        val onEdit: (Task.Id) -> Unit,
    ) : FilesItem
}