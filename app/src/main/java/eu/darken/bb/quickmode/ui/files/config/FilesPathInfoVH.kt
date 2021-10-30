package eu.darken.bb.quickmode.ui.files.config

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.databinding.QuickmodeFilesConfigPathItemBinding
import eu.darken.bb.quickmode.ui.common.config.ConfigAdapter

class FilesPathInfoVH(parent: ViewGroup) :
    ConfigAdapter.BaseVH<FilesPathInfoVH.Item, QuickmodeFilesConfigPathItemBinding>(
        R.layout.quickmode_files_config_path_item,
        parent
    ) {

    private val adapter = FileSourceAdapter()

    override val viewBinding = lazy { QuickmodeFilesConfigPathItemBinding.bind(itemView) }

    override val onBindData: QuickmodeFilesConfigPathItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    data class Item(
        val sources: List<Generator>,
        val onAdd: () -> Unit,
        val onRemove: (Generator.Id) -> Unit,
    ) : ConfigAdapter.Item {
        override val stableId: Long = this.hashCode().toLong()
    }
}