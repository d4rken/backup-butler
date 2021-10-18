package eu.darken.bb.task.ui.editor

import android.os.Parcelable
import eu.darken.bb.task.core.Task
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskEditorArgs(
    val taskId: Task.Id? = null,
    val taskType: Task.Type,
) : Parcelable