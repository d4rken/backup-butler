package eu.darken.bb.quickmode.ui.wizard.files

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.databinding.QuickmodeWizardFilesPathItemBinding
import eu.darken.bb.quickmode.ui.wizard.common.WizardAdapter

class FilesPathInfoVH(parent: ViewGroup) :
    WizardAdapter.BaseVH<FilesPathInfoVH.Item, QuickmodeWizardFilesPathItemBinding>(
        R.layout.quickmode_wizard_files_path_item,
        parent
    ) {

    private val adapter = FileSourceAdapter()

    override val viewBinding = lazy { QuickmodeWizardFilesPathItemBinding.bind(itemView) }

    override val onBindData: QuickmodeWizardFilesPathItemBinding.(
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