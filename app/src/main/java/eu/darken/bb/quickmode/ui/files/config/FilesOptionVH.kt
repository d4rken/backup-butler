package eu.darken.bb.quickmode.ui.files.config

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.databinding.QuickmodeFilesConfigOptionItemBinding
import eu.darken.bb.quickmode.ui.common.config.ConfigAdapter

class FilesOptionVH(parent: ViewGroup) :
    ConfigAdapter.BaseVH<FilesOptionVH.Item, QuickmodeFilesConfigOptionItemBinding>(
        R.layout.quickmode_files_config_option_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeFilesConfigOptionItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeFilesConfigOptionItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        optionOverwrite.isChecked = item.replaceExisting
        optionOverwrite.setOnCheckedChangeListener { button, isChecked ->
            item.replaceExistingOnToggle(isChecked)
        }
    }

    data class Item(
        val replaceExisting: Boolean = false,
        val replaceExistingOnToggle: (Boolean) -> Unit,
    ) : ConfigAdapter.Item {
        override val stableId: Long = "FilesOptionVH".hashCode().toLong()
    }
}