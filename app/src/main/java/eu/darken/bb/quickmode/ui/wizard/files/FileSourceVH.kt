package eu.darken.bb.quickmode.ui.wizard.files

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.databinding.QuickmodeWizardFilesPathAdapterItemBinding

class FileSourceVH(parent: ViewGroup) :
    FileSourceAdapter.BaseVH<FileSourceVH.Item, QuickmodeWizardFilesPathAdapterItemBinding>(
        R.layout.quickmode_wizard_files_path_adapter_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeWizardFilesPathAdapterItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeWizardFilesPathAdapterItemBinding.(
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