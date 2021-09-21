package eu.darken.bb.storage.ui.viewer.content.page

import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.databinding.StorageViewerItemContentEntryAdapterLineBinding
import javax.inject.Inject


class ContentEntryAdapter @Inject constructor() : ModularAdapter<ContentEntryAdapter.VH>(),
    DataAdapter<Backup.ContentInfo.Entry> {

    override val data = mutableListOf<Backup.ContentInfo.Entry>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it) })
    }

    override fun getItemCount(): Int = data.size

    class VH(parent: ViewGroup) : ModularAdapter.VH(R.layout.storage_viewer_item_content_entry_adapter_line, parent),
        BindableVH<Backup.ContentInfo.Entry, StorageViewerItemContentEntryAdapterLineBinding> {

        override val viewBinding: Lazy<StorageViewerItemContentEntryAdapterLineBinding> = lazy {
            StorageViewerItemContentEntryAdapterLineBinding.bind(itemView)
        }

        override val onBindData: StorageViewerItemContentEntryAdapterLineBinding.(
            item: Backup.ContentInfo.Entry,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
            val (cap, desc) = item.labeling
            caption.text = cap.get(context)
            caption.setGone(cap.isEmpty(context))
            description.text = desc.get(context)
        }
    }
}