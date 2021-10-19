package eu.darken.bb.quickmode.ui.wizard.common

import android.annotation.SuppressLint
import android.text.format.Formatter
import android.view.ViewGroup
import eu.darken.bb.R
import eu.darken.bb.common.errors.localized
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.databinding.QuickmodeWizardCommonStorageInfoItemBinding
import eu.darken.bb.storage.core.Storage

class StorageInfoVH(parent: ViewGroup) :
    WizardAdapter.BaseVH<StorageInfoVH.Item, QuickmodeWizardCommonStorageInfoItemBinding>(
        R.layout.quickmode_wizard_common_storage_info_item,
        parent
    ) {

    override val viewBinding = lazy {
        QuickmodeWizardCommonStorageInfoItemBinding.bind(itemView)
    }

    override val onBindData: QuickmodeWizardCommonStorageInfoItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        val storageId = item.infoOpt.storageId

        requireNotNull(item.infoOpt)

        val info = item.infoOpt.info!!

        when {
            info.config != null && info.isFinished -> primaryInfo.text = info.config.label
            info.isFinished -> primaryInfo.setText(R.string.general_error_label)
            else -> primaryInfo.setText(R.string.progress_loading_label)
        }

        when {
            info.error != null -> {
                primaryInfo.setTextColor(getColor(R.color.colorError))
                info.error.localized(context).let {
                    primaryInfo.text = it.label
                    secondaryInfo.text = it.description
                }
            }
            info.status != null -> {
                secondaryInfo.setTextColor(context.getColorForAttr(android.R.attr.textColorSecondary))
                @SuppressLint("SetTextI18n")
                secondaryInfo.text = "${getQuantityString(R.plurals.x_items, info.status.itemCount)}; ${
                    Formatter.formatFileSize(
                        context,
                        info.status.totalSize
                    )
                }"
                if (info.status.isReadOnly) secondaryInfo.append("; " + getString(R.string.general_read_only_label))
            }
            else -> {
                secondaryInfo.setTextColor(context.getColorForAttr(android.R.attr.textColorSecondary))
                secondaryInfo.text = null
            }
        }

        removeAction.setOnClickListener { item.onRemove(storageId) }
    }

    data class Item(
        val infoOpt: Storage.InfoOpt,
        val onRemove: (Storage.Id) -> Unit,
    ) : StorageItem
}