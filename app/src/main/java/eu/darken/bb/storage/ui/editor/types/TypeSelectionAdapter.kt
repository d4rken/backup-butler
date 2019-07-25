package eu.darken.bb.storage.ui.editor.types

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataBinderModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.SimpleVHCreator
import eu.darken.bb.storage.core.BackupStorage
import javax.inject.Inject


class TypeSelectionAdapter @Inject constructor() : ModularAdapter<TypeSelectionAdapter.VH>() {

    val data = mutableListOf<BackupStorage.Type>()

    init {
        modules.add(DataBinderModule<BackupStorage.Type, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.storageeditor_typeselection_adapter_line, parent), BindableVH<BackupStorage.Type> {
        @BindView(R.id.icon) lateinit var icon: ImageView
        @BindView(R.id.name) lateinit var label: TextView
        @BindView(R.id.description) lateinit var description: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: BackupStorage.Type) {
            icon.setImageResource(item.iconRes)
            label.setText(item.labelRes)
            description.setText(item.descriptionRes)
        }

    }
}