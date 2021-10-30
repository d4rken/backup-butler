package eu.darken.bb.quickmode.ui.files

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeMainFilesCreateItemBinding
import eu.darken.bb.quickmode.ui.QuickModeAdapter

class FilesInfoCreateVH(parent: ViewGroup) :
    QuickModeAdapter.BaseVH<FilesInfoCreateVH.Item, QuickmodeMainFilesCreateItemBinding>(
        R.layout.quickmode_main_files_create_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeMainFilesCreateItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeMainFilesCreateItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        createAction.setOnClickListener { item.onCreateAppsTaskAction() }
    }

    data class Item(
        val onCreateAppsTaskAction: () -> Unit,
    ) : FilesItem
}