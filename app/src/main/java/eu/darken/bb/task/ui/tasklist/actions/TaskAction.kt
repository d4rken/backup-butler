package eu.darken.bb.task.ui.tasklist.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import eu.darken.bb.R

enum class TaskAction constructor(
        @DrawableRes val iconRes: Int,
        @StringRes val labelRes: Int
) {
    RUN(R.drawable.ic_play_arrow, R.string.action_run),
    EDIT(R.drawable.ic_mode_edit, R.string.action_edit),
    DELETE(R.drawable.ic_delete, R.string.action_delete)
}