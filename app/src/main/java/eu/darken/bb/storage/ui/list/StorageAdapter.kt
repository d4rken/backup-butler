package eu.darken.bb.storage.ui.list

import android.annotation.SuppressLint
import android.text.format.Formatter
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.airbnb.lottie.LottieAnimationView
import eu.darken.bb.R
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.lists.*
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.storage.core.Storage
import javax.inject.Inject

class StorageAdapter @Inject constructor()
    : ModularAdapter<StorageAdapter.VH>(), DataAdapter<Storage.InfoOpt> {

    override val data = mutableListOf<Storage.InfoOpt>()

    init {
        modules.add(DataBinderModule<Storage.InfoOpt, VH>(data))
        modules.add(SimpleVHCreator { VH(it) })
    }

    override fun getItemCount(): Int = data.size


    class VH(parent: ViewGroup)
        : ModularAdapter.VH(R.layout.storage_list_adapter_line, parent), BindableVH<Storage.InfoOpt> {

        @BindView(R.id.type_label) lateinit var typeLabel: TextView
        @BindView(R.id.type_icon) lateinit var typeIcon: ImageView
        @BindView(R.id.label) lateinit var labelText: TextView
        @BindView(R.id.repo_status) lateinit var statusText: TextView
        @BindView(R.id.loading_animation) lateinit var loadingAnimation: LottieAnimationView

        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: Storage.InfoOpt) {
            if (item.info == null) {
                typeLabel.setText(R.string.general_unknown_label)
                typeIcon.setColorFilter(getColor(R.color.colorError))
                typeIcon.setImageResource(R.drawable.ic_error_outline)

                labelText.text = "?"

                statusText.text = getString(R.string.general_error_cant_access_msg, item.storageId)
                return
            }

            val info = item.info
            typeLabel.setText(info.storageType.labelRes)
            typeIcon.setImageResource(info.storageType.iconRes)
            typeIcon.setColorFilter(context.getColorForAttr(android.R.attr.textColorSecondary))

            if (info.config != null) {
                labelText.text = info.config.label
            } else {
                labelText.setText(R.string.progress_loading_label)
            }

            when {
                info.error != null -> {
                    statusText.setTextColor(getColor(R.color.colorError))
                    statusText.text = info.error.tryLocalizedErrorMessage(context)
                }
                info.status != null -> {
                    statusText.setTextColor(context.getColorForAttr(android.R.attr.textColorSecondary))
                    @SuppressLint("SetTextI18n")
                    statusText.text = "${getQuantityString(R.plurals.x_items, info.status.itemCount)}; ${Formatter.formatFileSize(context, info.status.totalSize)}"
                    if (info.status.isReadOnly) statusText.append("; " + getString(R.string.general_read_only_label))
                }
                else -> {
                    statusText.setTextColor(context.getColorForAttr(android.R.attr.textColorSecondary))
                    statusText.text = null
                }
            }

            loadingAnimation.setGone(info.isFinished)
        }
    }

}
