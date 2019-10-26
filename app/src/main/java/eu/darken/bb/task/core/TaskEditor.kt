package eu.darken.bb.task.core

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface TaskEditor {
    val editorData: Observable<out Data>

    fun load(task: Task): Completable

    fun save(): Single<out Task>

    fun isValidTask(): Observable<Boolean>

    fun updateLabel(label: String)

    interface Factory<EditorT : TaskEditor> {
        fun create(taskId: Task.Id): EditorT
    }

    interface Data {
        val taskId: Task.Id
        val label: String
        val isExistingTask: Boolean
        val isOneTimeTask: Boolean
    }

}