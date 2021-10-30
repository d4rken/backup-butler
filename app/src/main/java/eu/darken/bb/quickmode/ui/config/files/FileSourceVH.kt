package eu.darken.bb.quickmode.ui.config.files

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.databinding.QuickmodeFilesConfigPathAdapterItemBinding

class FileSourceVH(parent: ViewGroup) :
    FileSourceAdapter.BaseVH<FileSourceVH.Item, QuickmodeFilesConfigPathAdapterItemBinding>(
        R.layout.quickmode_files_config_path_adapter_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeFilesConfigPathAdapterItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeFilesConfigPathAdapterItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    data class Item(
        val generatorConfig: Generator.Config,
        val onEdit: (Generator.Id) -> Unit,
        val onRemove: (Generator.Id) -> Unit,
    ) : FileSourceAdapter.Item {
        override val stableId: Long = generatorConfig.generatorId.hashCode().toLong()
    }
}