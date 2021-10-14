package eu.darken.bb.main.ui.simple.cards.files

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.SimpleModeMainFilesLoadingItemBinding
import eu.darken.bb.main.ui.simple.SimpleModeAdapter

class FilesInfoLoadingVH(parent: ViewGroup) :
    SimpleModeAdapter.BaseVH<FilesInfoLoadingVH.Item, SimpleModeMainFilesLoadingItemBinding>(
        R.layout.simple_mode_main_files_loading_item,
        parent
    ) {

    override val viewBinding = lazy {
        SimpleModeMainFilesLoadingItemBinding.bind(itemView)
    }

    override val onBindData: SimpleModeMainFilesLoadingItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    object Item : SimpleModeAdapter.Item {
        override val stableId: Long = FilesInfoVH.LIST_ID
    }
}