package eu.darken.bb.backup.ui.generator.list.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import eu.darken.bb.R

enum class GeneratorsAction constructor(
        @DrawableRes val iconRes: Int,
        @StringRes val labelRes: Int
) {
    EDIT(R.drawable.ic_mode_edit, R.string.general_edit_action),
    DELETE(R.drawable.ic_delete, R.string.general_delete_action)
}