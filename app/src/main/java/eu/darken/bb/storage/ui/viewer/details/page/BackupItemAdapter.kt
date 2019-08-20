package eu.darken.bb.storage.ui.viewer.details.page

import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.lists.*
import eu.darken.bb.storage.core.Storage
import javax.inject.Inject


class BackupItemAdapter @Inject constructor()
    : ModularAdapter<BackupItemAdapter.VH>(), DataAdapter<Storage.Content.Item> {

    override val data = mutableListOf<Storage.Content.Item>()

    init {
        modules.add(DataBinderModule<Storage.Content.Item, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.storage_viewer_detailpage_adapter_line, parent), BindableVH<Storage.Content.Item> {
        //        @BindView(R.id.icon) lateinit var icon: ImageView
        @BindView(R.id.label) lateinit var label: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Storage.Content.Item) {
            label.text = item.label
        }

    }
}