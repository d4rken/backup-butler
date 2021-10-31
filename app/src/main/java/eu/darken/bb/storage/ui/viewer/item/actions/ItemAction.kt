package eu.darken.bb.storage.ui.viewer.item.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import eu.darken.bb.R
import eu.darken.bb.common.DialogActionEnum

enum class ItemAction constructor(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int
) : DialogActionEnum {
    VIEW(R.drawable.ic_eye, R.string.general_view_action),
    RESTORE(R.drawable.ic_restore_onprimary, R.string.general_restore_action),
    DELETE(R.drawable.ic_delete, R.string.general_delete_action)
}