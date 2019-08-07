package eu.darken.bb.task.core

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.opt
import eu.darken.bb.storage.core.StorageRefRepo
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

@PerApp
class BackupTaskRepo @Inject constructor(
        @AppContext context: Context,
        val moshi: Moshi
) {
    private val taskAdapter = moshi.adapter(Task::class.java)
    private val preferences: SharedPreferences = context.getSharedPreferences("backup_tasks", Context.MODE_PRIVATE)
    private val tasksPub = BehaviorSubject.create<Map<Task.Id, Task>>()
    private val internalTasks = mutableMapOf<Task.Id, Task>()

    val tasks: Observable<Map<Task.Id, Task>> = tasksPub.hide()

    init {
        preferences.all.forEach {
            val task = taskAdapter.fromJson(it.value as String)!!
            internalTasks[task.taskId] = task
        }
        tasksPub.onNext(internalTasks)
    }

    fun get(id: Task.Id): Single<Opt<Task>> = tasks
            .firstOrError()
            .map { Opt(it[id]) }

    // Puts the task into storage, returns the previous value
    @Synchronized fun put(task: Task): Single<Opt<Task>> = Single.fromCallable {
        val old = internalTasks.put(task.taskId, task)
        Timber.tag(TAG).d("put(task=%s) -> old=%s", task, old)
        update()
        return@fromCallable old.opt()
    }

    @Synchronized fun remove(taskId: Task.Id): Single<Opt<Task>> = Single.fromCallable {
        val old = internalTasks.remove(taskId)
        Timber.tag(TAG).d("remove(taskId=%s) -> old=%s", taskId, old)
        update()
        if (old == null) Timber.tag(StorageRefRepo.TAG).w("Tried to delete non-existant Task: %s", taskId)
        return@fromCallable old.opt()
    }

    @Synchronized private fun update() {
        preferences.edit().clear().apply()
        internalTasks.values.forEach {
            preferences.edit().putString("${it.taskId}", taskAdapter.toJson(it)).apply()
        }
        tasksPub.onNext(internalTasks)
    }

    companion object {
        val TAG = App.logTag("Task", "Repo")
    }
}