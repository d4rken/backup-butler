package eu.darken.bb.backups.ui.generator.list.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import eu.darken.bb.R

enum class GeneratorsAction constructor(
        @DrawableRes val iconRes: Int,
        @StringRes val labelRes: Int
) {
    EDIT(R.drawable.ic_mode_edit, R.string.action_edit),
    DELETE(R.drawable.ic_delete, R.string.action_delete)
}