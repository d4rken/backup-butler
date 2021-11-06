package eu.darken.bb.task.core

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface TaskEditor {
    val editorData: Observable<out Data>

    fun load(task: Task): Completable

    fun snapshot(): Single<out Task>

    fun isValid(): Observable<Boolean>

    fun updateLabel(label: String)

    interface Factory<EditorT : TaskEditor> {
        fun create(taskId: Task.Id): EditorT
    }

    interface Data {
        val taskId: Task.Id
        val label: String
        val isExistingTask: Boolean
        val isOneTimeUse: Boolean
    }

}