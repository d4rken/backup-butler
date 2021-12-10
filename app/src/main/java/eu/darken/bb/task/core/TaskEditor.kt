package eu.darken.bb.task.core

import kotlinx.coroutines.flow.Flow

interface TaskEditor {
    val editorData: Flow<out Data>

    suspend fun load(task: Task)

    suspend fun snapshot(): Task

    fun isValid(): Flow<Boolean>

    suspend fun updateLabel(label: String)

    interface Factory<EditorT : TaskEditor> {
        fun create(taskId: Task.Id): EditorT
    }

    interface Data {
        val taskId: Task.Id
        val label: String
        val isExistingTask: Boolean
        val isSingleUse: Boolean
    }

}