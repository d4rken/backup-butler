package eu.darken.bb.storage.ui.viewer.content.page

import android.view.ViewGroup
import androidx.core.view.isGone
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.databinding.StorageViewerContentItemAdapterLineBinding
import javax.inject.Inject


class ContentItemAdapter @Inject constructor() : ModularAdapter<ContentItemAdapter.VH>(),
    DataAdapter<Backup.ContentInfo.Entry> {

    override val data = mutableListOf<Backup.ContentInfo.Entry>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup) : ModularAdapter.VH(R.layout.storage_viewer_content_item_adapter_line, parent),
        BindableVH<Backup.ContentInfo.Entry, StorageViewerContentItemAdapterLineBinding> {

        override val viewBinding: Lazy<StorageViewerContentItemAdapterLineBinding> = lazy {
            StorageViewerContentItemAdapterLineBinding.bind(itemView)
        }

        override val onBindData: StorageViewerContentItemAdapterLineBinding.(
            item: Backup.ContentInfo.Entry,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
            val (cap, desc) = item.labeling
            caption.text = cap.get(context)
            caption.isGone = cap.isEmpty(context)
            description.text = desc.get(context)
        }
    }
}