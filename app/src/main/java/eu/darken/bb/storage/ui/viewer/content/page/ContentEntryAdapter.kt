package eu.darken.bb.storage.ui.viewer.content.page

import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.lists.*
import eu.darken.bb.storage.core.Storage
import javax.inject.Inject


class ContentEntryAdapter @Inject constructor()
    : ModularAdapter<ContentEntryAdapter.VH>(), DataAdapter<Storage.Item.Content.Entry> {

    override val data = mutableListOf<Storage.Item.Content.Entry>()

    init {
        modules.add(DataBinderModule<Storage.Item.Content.Entry, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.storage_viewer_item_content_entry_adapter_line, parent), BindableVH<Storage.Item.Content.Entry> {
        //        @BindView(R.id.icon) lateinit var icon: ImageView
        @BindView(R.id.label) lateinit var label: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Storage.Item.Content.Entry) {
            label.text = item.label
        }

    }
}