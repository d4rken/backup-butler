package eu.darken.bb.storage.ui.list

import android.annotation.SuppressLint
import android.text.format.Formatter
import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.common.errors.localized
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.lists.*
import eu.darken.bb.common.lists.modular.ModularAdapter
import eu.darken.bb.common.lists.modular.mods.DataBinderMod
import eu.darken.bb.common.lists.modular.mods.SimpleVHCreatorMod
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.databinding.StorageListAdapterLineBinding
import eu.darken.bb.storage.core.Storage
import javax.inject.Inject

class StorageAdapter @Inject constructor() : ModularAdapter<StorageAdapter.VH>(), DataAdapter<Storage.InfoOpt> {

    override val data = mutableListOf<Storage.InfoOpt>()

    init {
        modules.add(DataBinderMod(data))
        modules.add(SimpleVHCreatorMod { VH(it) })
    }

    override fun getItemCount(): Int = data.size


    class VH(parent: ViewGroup) : ModularAdapter.VH(R.layout.storage_list_adapter_line, parent),
        BindableVH<Storage.InfoOpt, StorageListAdapterLineBinding> {

        override val viewBinding: Lazy<StorageListAdapterLineBinding> = lazy {
            StorageListAdapterLineBinding.bind(itemView)
        }

        override val onBindData: StorageListAdapterLineBinding.(
            item: Storage.InfoOpt,
            payloads: List<Any>
        ) -> Unit = onBindData@{ item, _ ->
            if (item.info == null) {
                typeLabel.setText(R.string.general_unknown_label)
                typeIcon.setColorFilter(getColor(R.color.colorError))
                typeIcon.setImageResource(R.drawable.ic_error_outline)

                label.text = "?"

                repoStatus.text = getString(R.string.general_error_cant_access_msg, item.storageId)
                return@onBindData
            }

            val info = item.info
            typeLabel.setText(info.storageType.labelRes)
            typeIcon.setImageResource(info.storageType.iconRes)
            typeIcon.setColorFilter(context.getColorForAttr(android.R.attr.textColorSecondary))

            when {
                info.config != null && info.isFinished -> label.text = info.config.label
                info.isFinished -> label.setText(R.string.general_error_label)
                else -> label.setText(R.string.progress_loading_label)
            }

            when {
                info.error != null -> {
                    label.setTextColor(getColor(R.color.colorError))
                    info.error.localized(context).let {
                        label.text = it.label
                        repoStatus.text = it.description
                    }
                }
                info.status != null -> {
                    repoStatus.setTextColor(context.getColorForAttr(android.R.attr.textColorSecondary))
                    @SuppressLint("SetTextI18n")
                    repoStatus.text = "${getQuantityString(R.plurals.x_items, info.status.itemCount)}; ${
                        Formatter.formatFileSize(
                            context,
                            info.status.totalSize
                        )
                    }"
                    if (info.status.isReadOnly) repoStatus.append("; " + getString(R.string.general_read_only_label))
                }
                else -> {
                    repoStatus.setTextColor(context.getColorForAttr(android.R.attr.textColorSecondary))
                    repoStatus.text = null
                }
            }

            loadingAnimation.setGone(info.isFinished)
        }
    }

}
