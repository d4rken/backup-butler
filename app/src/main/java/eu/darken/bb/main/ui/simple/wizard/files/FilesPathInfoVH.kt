package eu.darken.bb.main.ui.simple.wizard.files

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.databinding.SimpleModeWizardFilesPathItemBinding
import eu.darken.bb.main.ui.simple.wizard.common.WizardAdapter

class FilesPathInfoVH(parent: ViewGroup) :
    WizardAdapter.BaseVH<FilesPathInfoVH.Item, SimpleModeWizardFilesPathItemBinding>(
        R.layout.simple_mode_wizard_files_path_item,
        parent
    ) {

    private val adapter = SimpleFileSourceAdapter()

    override val viewBinding = lazy { SimpleModeWizardFilesPathItemBinding.bind(itemView) }

    override val onBindData: SimpleModeWizardFilesPathItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }

    data class Item(
        val sources: List<Generator>,
        val onAdd: () -> Unit,
        val onRemove: (Generator.Id) -> Unit,
    ) : WizardAdapter.Item {
        override val stableId: Long = this.hashCode().toLong()
    }
}