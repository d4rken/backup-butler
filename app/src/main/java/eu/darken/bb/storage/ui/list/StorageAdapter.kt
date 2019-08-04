package eu.darken.bb.storage.ui.list

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.lists.BindableVH
import eu.darken.bb.common.lists.DataBinderModule
import eu.darken.bb.common.lists.ModularAdapter
import eu.darken.bb.common.lists.SimpleVHCreator
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.storage.core.StorageInfo
import eu.darken.bb.tasks.ui.editor.destinations.DestinationsAdapter
import javax.inject.Inject

class StorageAdapter @Inject constructor() : ModularAdapter<DestinationsAdapter.VH>() {

    val data = mutableListOf<StorageInfo>()

    init {
        modules.add(DataBinderModule<StorageInfo, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size


    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.storage_list_adapter_line, parent), BindableVH<StorageInfo> {

        @BindView(R.id.type_label) lateinit var typeLabel: TextView
        @BindView(R.id.type_icon) lateinit var typeIcon: ImageView
        @BindView(R.id.label) lateinit var repoLabel: TextView
        @BindView(R.id.repo_status) lateinit var repoStatus: TextView


        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: StorageInfo) {
            typeLabel.setText(item.ref.storageType.labelRes)
            typeIcon.setImageResource(item.ref.storageType.iconRes)

            if (item.config != null) {
                repoLabel.text = item.config.label
            }

            if (item.status != null) {
                repoStatus.setTextColor(context.getColorForAttr(android.R.attr.textColorSecondary))
                repoStatus.text = "Count: TODO; Size: TODO"
            }

            if (item.error != null) {
                repoStatus.setTextColor(getColor(R.color.colorError))
                repoStatus.text = item.error.tryLocalizedErrorMessage(context)
            }
        }

    }
}