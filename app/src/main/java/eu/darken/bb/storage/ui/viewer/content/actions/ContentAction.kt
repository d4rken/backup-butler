package eu.darken.bb.storage.ui.viewer.content.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import eu.darken.bb.R

enum class ContentAction constructor(
        @DrawableRes val iconRes: Int,
        @StringRes val labelRes: Int
) {
    VIEW(R.drawable.ic_eye, R.string.action_view),
    DELETE(R.drawable.ic_delete, R.string.action_delete)
}