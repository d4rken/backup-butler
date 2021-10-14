package eu.darken.bb.main.ui.simple.wizard.files

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.databinding.SimpleModeWizardFilesPathAdapterItemBinding

class SimpleFileSourceVH(parent: ViewGroup) :
    SimpleFileSourceAdapter.BaseVH<SimpleFileSourceVH.Item, SimpleModeWizardFilesPathAdapterItemBinding>(
        R.layout.simple_mode_wizard_files_path_adapter_item,
        parent
    ) {

    override val viewBinding = lazy {
        SimpleModeWizardFilesPathAdapterItemBinding.bind(itemView)
    }

    override val onBindData: SimpleModeWizardFilesPathAdapterItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    data class Item(
        val generatorConfig: Generator.Config,
        val onEdit: (Generator.Id) -> Unit,
        val onRemove: (Generator.Id) -> Unit,
    ) : SimpleFileSourceAdapter.Item {
        override val stableId: Long = generatorConfig.generatorId.hashCode().toLong()
    }
}