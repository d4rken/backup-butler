package eu.darken.bb.tasks.ui.tasklist.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import eu.darken.bb.R

enum class TaskActions constructor(
        @DrawableRes val iconRes: Int,
        @StringRes val labelRes: Int
) {
    RUN(R.drawable.ic_play_arrow, R.string.button_run),
    EDIT(R.drawable.ic_mode_edit, R.string.button_edit),
    DELETE(R.drawable.ic_delete, R.string.button_delete)
}