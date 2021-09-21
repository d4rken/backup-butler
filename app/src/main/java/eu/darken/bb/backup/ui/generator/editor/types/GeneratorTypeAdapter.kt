package eu.darken.bb.backup.ui.generator.editor.types

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.databinding.GeneratorEditorTypeselectionAdapterLineBinding
import javax.inject.Inject


class GeneratorTypeAdapter @Inject constructor() : ModularAdapter<GeneratorTypeAdapter.VH>(), DataAdapter<Backup.Type> {

    override val data = mutableListOf<Backup.Type>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup) : ModularAdapter.VH(R.layout.generator_editor_typeselection_adapter_line, parent),
        BindableVH<Backup.Type, GeneratorEditorTypeselectionAdapterLineBinding> {

        override val viewBinding = lazy { GeneratorEditorTypeselectionAdapterLineBinding.bind(itemView) }

        override val onBindData: GeneratorEditorTypeselectionAdapterLineBinding.(
            item: Backup.Type,
            payloads: List<Any>,
        ) -> Unit = { item, _ ->
            icon.setImageResource(item.iconRes)
            name.setText(item.labelRes)
            description.setText(item.descriptionRes)
        }
    }
}