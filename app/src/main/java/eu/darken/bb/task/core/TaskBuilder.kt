package eu.darken.bb.task.core

import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.Logging.Priority.WARN
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskBuilder @Inject constructor(
    private val taskRepo: TaskRepo,
    private val editors: @JvmSuppressWildcards Map<Task.Type, TaskEditor.Factory<out TaskEditor>>,
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) {

    private val state = DynamicStateFlow<Map<Task.Id, Data>>(TAG, appScope) { mutableMapOf() }

    fun task(id: Task.Id): Flow<Data> = state.flow
        .filter { it.containsKey(id) }
        .map { it[id]!! }

    suspend fun update(id: Task.Id, action: (Data?) -> Data?): Data? {
        val new = state.updateBlocking {

            val mutMap = this.toMutableMap()
            val oldTask = mutMap.remove(id)
            val newTask = action.invoke(oldTask)
            if (newTask != null) {
                mutMap[newTask.taskId] = newTask
            }
            mutMap.toMap()
        }

        Timber.tag(TAG).v("Task updated: %s (%s): %s", id, action, new)

        return new[id]
    }


    suspend fun remove(id: Task.Id): Data? {
        val old = state.value()[id]

        update(id) {
            null
        }

        Timber.tag(TAG).v("Removed task: %s", id)
        return old
    }

    suspend fun save(id: Task.Id): Task {
        Timber.tag(TAG).d("Saving %s", id)
        val removed = remove(id)
        requireNotNull(removed) { "Can't find ID to save: $id" }
        checkNotNull(removed.editor) { "Can't save builder data NULL editor: $removed" }

        val task = removed.editor.snapshot()
        val previousTask = taskRepo.put(task)
        if (previousTask != null) log(TAG, WARN) { "Replaced previous task: $previousTask" }

        log(TAG) { "Saved $id -> $task" }
        return task
    }

    /**
     * Attempt to load an existing task into an editor
     */
    private suspend fun load(id: Task.Id): Data? {
        val task = taskRepo.get(id)
        if (task == null) {
            log(TAG) { "No task found for $id" }
            return null
        }

        val editor = editors.getValue(task.taskType).create(task.taskId)
        editor.load(task)

        val data = Data(
            taskId = task.taskId,
            taskType = task.taskType,
            editor = editor
        )
        update(id) { data }

        return data
    }

    /**
     * Attempts to load an existing task into an editor,
     * otherwise creates a new editor.
     */
    suspend fun getEditor(
        taskId: Task.Id = Task.Id(),
        type: Task.Type? = null,
        createNew: Boolean = true,
    ): Data {
        state.value()[taskId]?.let {
            log(TAG) { "getEditor(taskId=$taskId, type=$type): Returning cached task editor: $it" }
            return it
        }

        load(taskId)?.let {
            log(TAG) { "getEditor(taskId=$taskId, type=$type):  Created editor for existing task: $it" }
            return it
        }

        requireNotNull(type) { "If load($taskId) fails, a type needs to be specified." }
        log(TAG) { "Creating new editor for $taskId" }
        val newData = Data(
            taskId = taskId,
            taskType = type,
            editor = editors.getValue(type).create(taskId),
        )
        update(taskId) { newData }

        return newData
    }

    data class Data(
        val taskId: Task.Id,
        val taskType: Task.Type,
        val editor: TaskEditor? = null
    )

    companion object {
        val TAG = logTag("Task", "Builder")
    }
}