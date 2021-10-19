package eu.darken.bb.task.ui.editor

import android.os.Parcelable
import eu.darken.bb.task.core.Task
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskEditorArgs constructor(
    val taskId: Task.Id?,
    val taskType: Task.Type?,
) : Parcelable {
    constructor(taskId: Task.Id?) : this(taskId = taskId, taskType = null)
    constructor(taskType: Task.Type?) : this(taskId = null, taskType = taskType)
}