package eu.darken.bb.task.ui.tasklist.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import eu.darken.bb.R

enum class TaskAction constructor(
        @DrawableRes val iconRes: Int,
        @StringRes val labelRes: Int
) {
    RUN(R.drawable.ic_play_arrow, R.string.general_run_action),
    EDIT(R.drawable.ic_mode_edit, R.string.general_edit_action),
    DELETE(R.drawable.ic_delete, R.string.general_delete_action)
}