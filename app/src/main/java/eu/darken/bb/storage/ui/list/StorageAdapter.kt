package eu.darken.bb.storage.ui.list

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.lists.*
import eu.darken.bb.common.tryLocalizedErrorMessage
import javax.inject.Inject

class StorageAdapter @Inject constructor()
    : ModularAdapter<StorageAdapter.VH>(), DataAdapter<StorageInfoOpt> {

    override val data = mutableListOf<StorageInfoOpt>()

    init {
        modules.add(DataBinderModule<StorageInfoOpt, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size


    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.storage_list_adapter_line, parent), BindableVH<StorageInfoOpt> {

        @BindView(R.id.type_label) lateinit var typeLabel: TextView
        @BindView(R.id.type_icon) lateinit var typeIcon: ImageView
        @BindView(R.id.label) lateinit var labelText: TextView
        @BindView(R.id.repo_status) lateinit var statusText: TextView


        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: StorageInfoOpt) {
            if (item.info != null) {
                val info = item.info
                typeLabel.setText(info.ref.storageType.labelRes)
                typeIcon.setImageResource(info.ref.storageType.iconRes)
                typeIcon.setColorFilter(context.getColorForAttr(android.R.attr.textColorSecondary))

                labelText.text = info.config?.label ?: "?"

                when {
                    info.error != null -> {
                        statusText.setTextColor(getColor(R.color.colorError))
                        statusText.text = info.error.tryLocalizedErrorMessage(context)
                    }
                    info.status != null -> {
                        statusText.setTextColor(context.getColorForAttr(android.R.attr.textColorSecondary))
                        statusText.text = "Count: TODO; Size: TODO"
                    }
                    else -> {
                        statusText.setTextColor(context.getColorForAttr(android.R.attr.textColorSecondary))
                        statusText.text = null
                    }
                }
            } else {
                typeLabel.setText(R.string.label_unknown)
                typeIcon.setColorFilter(getColor(R.color.colorError))
                typeIcon.setImageResource(R.drawable.ic_error_outline)

                labelText.text = "?"

                statusText.text = getString(R.string.error_message_cant_find_x, item.storageId)
            }
        }
    }

}
