package eu.darken.bb.main.ui.simple.cards.files

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.SimpleModeMainFilesItemBinding
import eu.darken.bb.main.ui.simple.SimpleModeAdapter
import eu.darken.bb.task.core.Task

class FilesInfoVH(parent: ViewGroup) :
    SimpleModeAdapter.BaseVH<FilesInfoVH.Item, SimpleModeMainFilesItemBinding>(
        R.layout.simple_mode_main_files_item,
        parent
    ) {

    override val viewBinding = lazy { SimpleModeMainFilesItemBinding.bind(itemView) }

    override val onBindData: SimpleModeMainFilesItemBinding.(
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