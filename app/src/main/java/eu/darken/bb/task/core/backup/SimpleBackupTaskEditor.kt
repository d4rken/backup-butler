package eu.darken.bb.task.core.backup

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.HotData
import eu.darken.bb.common.debug.logging.Logging.Priority.WARN
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskEditor
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single


class SimpleBackupTaskEditor @AssistedInject constructor(
    @Assisted val taskId: Task.Id
) : TaskEditor {
    private val editorDataPub = HotData(tag = TAG) { Data(taskId = taskId) }
    override val editorData = editorDataPub.data

    override fun load(task: Task): Completable = Single.just(task as SimpleBackupTask)
        .flatMap { simpleTask ->
            require(taskId == simpleTask.taskId) { "IDs don't match" }
            editorDataPub.updateRx {
                it.copy(
                    label = simpleTask.label,
                    isExistingTask = true,
                    sources = simpleTask.sources,
                    destinations = simpleTask.destinations
                )
            }
        }
        .ignoreElement()

    override fun snapshot(): Single<out Task> = Single.fromCallable {
        val data = editorDataPub.snapshot
        SimpleBackupTask(
            taskId = data.taskId,
            label = data.label,
            sources = data.sources,
            destinations = data.destinations
        )
    }

    override fun isValid(): Observable<Boolean> = editorData.map { data ->
        data.label.isNotBlank()
    }

    override fun updateLabel(label: String) {
        editorDataPub.update {
            it.copy(label = label)
        }
    }

    fun addStorage(storageId: Storage.Id) {
        editorDataPub.update {
            if (it.destinations.contains(storageId)) {
                log(TAG, WARN) { "Storage.Id was already added as storage: $storageId" }
            }
            it.copy(destinations = it.destinations.toMutableSet().apply { add(storageId) }.toSet())
        }
    }

    fun removeStorage(storageId: Storage.Id) {
        editorDataPub.update { task ->
            if (!task.destinations.contains(storageId)) {
                log(TAG, WARN) { "Task ($taskId) does not contain storage $storageId" }
            }
            task.copy(destinations = task.destinations.toMutableSet().filterNot { it == storageId }.toSet())
        }
    }

    fun addGenerator(generatorId: Generator.Id) {
        editorDataPub.update {
            if (it.sources.contains(generatorId)) {
                log(TAG, WARN) { "Generator.Id was already added: $generatorId" }
            }
            it.copy(sources = it.sources.toMutableSet().apply { add(generatorId) }.toSet())
        }
    }

    fun removeGenerator(generatorId: Generator.Id) {
        editorDataPub.update { task ->
            if (task.sources.contains(generatorId)) {
                log(TAG, WARN) { "Task ($taskId) does not contain generator: $generatorId" }
            }
            task.copy(sources = task.sources.toMutableSet().filterNot { it == generatorId }.toSet())
        }
    }

    fun updateOneTime(isOneTimeUse: Boolean): Single<Data> = editorDataPub
        .updateRx { it.copy(isOneTimeUse = isOneTimeUse) }
        .map { it.newValue }

    data class Data(
        override val taskId: Task.Id,
        override val label: String = "",
        override val isExistingTask: Boolean = false,
        override val isOneTimeUse: Boolean = false,
        val sources: Set<Generator.Id> = emptySet(),
        val destinations: Set<Storage.Id> = emptySet()
    ) : TaskEditor.Data

    companion object {
        internal val TAG = logTag("Task", "Restore", "Editor", "Simple")
    }

    @AssistedFactory
    interface Factory : TaskEditor.Factory<SimpleBackupTaskEditor>
}