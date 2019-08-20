package eu.darken.bb.task.core

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface TaskEditor {
    val config: Observable<out Task>

    fun load(task: Task): Completable

    fun save(): Single<out Task>

    fun isValidTask(): Observable<Boolean>

    fun isExistingTask(): Boolean

    fun updateLabel(label: String)

    interface Factory<EditorT : TaskEditor> {
        fun create(taskId: Task.Id): EditorT
    }

}