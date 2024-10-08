package eu.darken.bb.processor.core

import android.os.Parcelable
import eu.darken.bb.task.core.Task
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProcessorRequest(
    val taskId: Task.Id,
    val retryAttempts: Int = 0,
) : Parcelable {
    val maxRunAttempts: Int
        get() = retryAttempts + 1
}