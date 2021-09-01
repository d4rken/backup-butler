package eu.darken.bb.storage.ui.list.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import eu.darken.bb.R

enum class StorageAction constructor(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int
) {
    VIEW(R.drawable.ic_eye, R.string.general_view_action),
    RESTORE(R.drawable.ic_restore_onprimary, R.string.general_restore_action),
    EDIT(R.drawable.ic_mode_edit, R.string.general_edit_action),
    DETACH(R.drawable.ic_eject, R.string.storage_detach_action),
    DELETE(R.drawable.ic_delete, R.string.general_delete_action)
}