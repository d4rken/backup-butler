package eu.darken.bb.storage.ui.list.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import eu.darken.bb.R

enum class StorageAction constructor(
        @DrawableRes val iconRes: Int,
        @StringRes val labelRes: Int
) {
    VIEW(R.drawable.ic_eye, R.string.action_view),
    EDIT(R.drawable.ic_mode_edit, R.string.action_edit),
    DELETE(R.drawable.ic_delete, R.string.action_delete)
}