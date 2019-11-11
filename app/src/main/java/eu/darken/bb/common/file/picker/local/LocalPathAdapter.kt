package eu.darken.bb.common.file.picker.local

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.lists.*
import javax.inject.Inject


class LocalPathAdapter @Inject constructor()
    : ModularAdapter<LocalPathAdapter.VH>(), DataAdapter<APath> {

    override val data = mutableListOf<APath>()

    init {
        modules.add(DataBinderModule<APath, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.storage_editor_typeselection_adapter_line, parent), BindableVH<APath> {
        @BindView(R.id.icon) lateinit var icon: ImageView
        @BindView(R.id.name) lateinit var label: TextView
        @BindView(R.id.description) lateinit var description: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: APath) {
            label.text = item.userReadablePath(context)
        }

    }
}