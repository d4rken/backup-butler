package eu.darken.bb.storage.ui.editor.types

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.databinding.StorageEditorTypeselectionAdapterLineBinding
import eu.darken.bb.storage.core.Storage
import javax.inject.Inject


class StorageTypeAdapter @Inject constructor() : ModularAdapter<StorageTypeAdapter.VH>(),
    DataAdapter<Storage.Type> {

    override val data = mutableListOf<Storage.Type>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup) : ModularAdapter.VH(R.layout.storage_editor_typeselection_adapter_line, parent),
        BindableVH<Storage.Type, StorageEditorTypeselectionAdapterLineBinding> {

        override val viewBinding: Lazy<StorageEditorTypeselectionAdapterLineBinding> = lazy {
            StorageEditorTypeselectionAdapterLineBinding.bind(itemView)
        }

        override val onBindData: StorageEditorTypeselectionAdapterLineBinding.(
            item: Storage.Type,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
            icon.setImageResource(item.iconRes)
            name.setText(item.labelRes)
            description.setText(item.descriptionRes)
        }
    }
}