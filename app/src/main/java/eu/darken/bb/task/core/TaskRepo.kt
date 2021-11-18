package eu.darken.bb.task.core

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.collections.mutate
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepo @Inject constructor(
    @ApplicationContext context: Context,
    val moshi: Moshi,
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider
) {
    private val taskAdapter = moshi.adapter(Task::class.java)
    private val preferences: SharedPreferences = context.getSharedPreferences("task_repo", Context.MODE_PRIVATE)

    private val internalData: DynamicStateFlow<Map<Task.Id, Task>> = DynamicStateFlow(TAG, appScope) {
        val initialData = mutableMapOf<Task.Id, Task>()
        preferences.all.forEach {
            val task = taskAdapter.fromJson(it.value as String)!!
            initialData[task.taskId] = task
        }
        initialData.toMap()
    }

    val tasks = internalData.flow

    init {
        // TODO use database instead of preferences
        internalData.flow
            .onEach { data ->
                preferences.edit().clear().apply()
                data.values.forEach {
                    preferences.edit().putString("${it.taskId}", taskAdapter.toJson(it)).apply()
                }
            }
            .launchIn(appScope)
    }

    suspend fun get(id: Task.Id): Task? = internalData.value()[id]

    // Puts the task into storage, returns the previous value
    suspend fun put(task: Task): Task? {
        var old: Task? = null
        internalData.updateBlocking {
            old = this[task.taskId]
            this.mutate {
                put(task.taskId, task)
            }
        }
        log(TAG) { "put(task=$task) -> old=$old" }
        return old
    }

    suspend fun remove(taskId: Task.Id): Task? {
        var removed: Task? = null
        internalData.updateBlocking {
            removed = this[taskId]
            this.toMutableMap().apply { remove(taskId) }
        }

        if (removed != null) {
            log(TAG) { "remove(taskId=$taskId) -> old=$removed" }
        } else {
            log(TAG) { "Tried to delete non-existant Task: $taskId" }
        }

        return removed
    }

    companion object {
        val TAG = logTag("Task", "Repo")
    }
}