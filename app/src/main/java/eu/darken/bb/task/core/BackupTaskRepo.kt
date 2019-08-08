package eu.darken.bb.task.core

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.opt
import eu.darken.bb.storage.core.StorageRefRepo
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@PerApp
class BackupTaskRepo @Inject constructor(
        @AppContext context: Context,
        val moshi: Moshi
) {
    private val taskAdapter = moshi.adapter(Task::class.java)
    private val preferences: SharedPreferences = context.getSharedPreferences("backup_tasks", Context.MODE_PRIVATE)
    private val internalData = HotData<Map<Task.Id, Task>> {
        val initialData = mutableMapOf<Task.Id, Task>()
        preferences.all.forEach {
            val task = taskAdapter.fromJson(it.value as String)!!
            initialData[task.taskId] = task
        }
        initialData
    }
    val tasks = internalData.data

    init {
        internalData.data
                .subscribeOn(Schedulers.io())
                .subscribe { data ->
                    preferences.edit().clear().apply()
                    data.values.forEach {
                        preferences.edit().putString("${it.taskId}", taskAdapter.toJson(it)).apply()
                    }
                }
    }

    fun get(id: Task.Id): Single<Opt<Task>> = tasks
            .firstOrError()
            .map { Opt(it[id]) }

    // Puts the task into storage, returns the previous value
    fun put(task: Task): Single<Opt<Task>> = internalData
            .updateRx { data ->
                data.toMutableMap().apply { put(task.taskId, task) }
            }
            .map { it.oldValue[task.taskId].opt() }
            .doOnSuccess { Timber.d("put(task=%s) -> old=%s", task, it.value) }

    fun remove(taskId: Task.Id): Single<Opt<Task>> = internalData
            .updateRx { data ->
                data.toMutableMap().apply { remove(taskId) }
            }
            .map { it.oldValue[taskId].opt() }
            .doOnSuccess {
                Timber.d("remove(taskId=%s) -> old=%s", taskId, it.value)
                if (it.isNull) Timber.tag(StorageRefRepo.TAG).w("Tried to delete non-existant Task: %s", taskId)
            }

    companion object {
        val TAG = App.logTag("Task", "Repo")
    }
}