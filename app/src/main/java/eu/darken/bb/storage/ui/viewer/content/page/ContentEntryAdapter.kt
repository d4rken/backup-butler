package eu.darken.bb.storage.ui.viewer.content.page

import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.lists.*
import javax.inject.Inject


class ContentEntryAdapter @Inject constructor()
    : ModularAdapter<ContentEntryAdapter.VH>(), DataAdapter<Backup.ContentInfo.Entry> {

    override val data = mutableListOf<Backup.ContentInfo.Entry>()

    init {
        modules.add(DataBinderModule<Backup.ContentInfo.Entry, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.storage_viewer_item_content_entry_adapter_line, parent), BindableVH<Backup.ContentInfo.Entry> {
        //        @BindView(R.id.icon) lateinit var icon: ImageView
        @BindView(R.id.label) lateinit var label: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Backup.ContentInfo.Entry) {
            label.text = item.getLabel(context)
        }

    }
}