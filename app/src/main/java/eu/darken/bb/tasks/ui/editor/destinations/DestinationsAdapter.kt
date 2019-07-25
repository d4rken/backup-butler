package eu.darken.bb.tasks.ui.editor.destinations

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
import eu.darken.bb.storage.core.StorageRef
import javax.inject.Inject


class DestinationsAdapter @Inject constructor() : ModularAdapter<DestinationsAdapter.VH>() {

    val data = mutableListOf<StorageRef>()

    init {
        modules.add(DataBinderModule<StorageRef, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.tasklist_adapter_line, parent), BindableVH<StorageRef> {
        @BindView(R.id.icon) lateinit var icon: ImageView
        @BindView(R.id.name) lateinit var label: TextView
        @BindView(R.id.description) lateinit var description: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: StorageRef) {
            icon.setImageResource(item.storageType.iconRes)
            label.setText(item.storageType.labelRes)
//            description.text = item.description
        }

    }
}