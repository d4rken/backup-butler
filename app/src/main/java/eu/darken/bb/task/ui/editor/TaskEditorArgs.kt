package eu.darken.bb.task.ui.editor

import android.os.Parcelable
import eu.darken.bb.task.core.Task
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskEditorArgs constructor(
    val taskId: Task.Id?,
    val taskType: Task.Type?,
    val isSingleUse: Boolean,
) : Parcelable {
    constructor(
        taskId: Task.Id?,
    ) : this(taskId = taskId, taskType = null, isSingleUse = false)

    constructor(
        taskType: Task.Type?,
        isSingleUse: Boolean
    ) : this(taskId = null, taskType = taskType, isSingleUse = isSingleUse)
}