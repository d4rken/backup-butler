package eu.darken.bb.quickmode.ui.files

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeMainFilesLoadingItemBinding
import eu.darken.bb.quickmode.ui.QuickModeAdapter

class QuickFilesLoadingVH(parent: ViewGroup) :
    QuickModeAdapter.BaseVH<QuickFilesLoadingVH.Item, QuickmodeMainFilesLoadingItemBinding>(
        R.layout.quickmode_main_files_loading_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeMainFilesLoadingItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeMainFilesLoadingItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    object Item : FilesItem
}