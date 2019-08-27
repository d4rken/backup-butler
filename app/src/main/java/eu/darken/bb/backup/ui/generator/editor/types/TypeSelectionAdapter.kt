package eu.darken.bb.backup.ui.generator.editor.types

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.lists.*
import javax.inject.Inject


class TypeSelectionAdapter @Inject constructor()
    : ModularAdapter<TypeSelectionAdapter.VH>(), DataAdapter<Generator.Type> {

    override val data = mutableListOf<Generator.Type>()

    init {
        modules.add(DataBinderModule<Generator.Type, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.generator_editor_typeselection_adapter_line, parent), BindableVH<Generator.Type> {
        @BindView(R.id.icon) lateinit var icon: ImageView
        @BindView(R.id.name) lateinit var label: TextView
        @BindView(R.id.description) lateinit var description: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Generator.Type) {
            icon.setImageResource(item.iconRes)
            label.setText(item.labelRes)
            description.setText(item.descriptionRes)
        }

    }
}