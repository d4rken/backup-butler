package eu.darken.bb.main.ui.simple.cards.files

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.SimpleModeMainFilesCreateItemBinding
import eu.darken.bb.main.ui.simple.SimpleModeAdapter

class FilesInfoCreateVH(parent: ViewGroup) :
    SimpleModeAdapter.BaseVH<FilesInfoCreateVH.Item, SimpleModeMainFilesCreateItemBinding>(
        R.layout.simple_mode_main_files_create_item,
        parent
    ) {

    override val viewBinding = lazy {
        SimpleModeMainFilesCreateItemBinding.bind(itemView)
    }

    override val onBindData: SimpleModeMainFilesCreateItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        createAction.setOnClickListener { item.onCreateAppsTaskAction() }
    }

    data class Item(
        val onCreateAppsTaskAction: () -> Unit,
    ) : FilesItem
}