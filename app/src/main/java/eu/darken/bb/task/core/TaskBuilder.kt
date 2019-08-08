package eu.darken.bb.task.core

import android.content.Context
import android.content.Intent
import eu.darken.bb.App
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.task.ui.editor.TaskEditorActivity
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@PerApp
class TaskBuilder @Inject constructor(
        @AppContext private val context: Context,
        private val taskRepo: BackupTaskRepo
) {

    private val hotData = HotData<Map<Task.Id, Task>>(mutableMapOf())

    fun task(id: Task.Id, create: (() -> Task)? = null): Observable<Task> {
        var consumed = create == null
        return hotData.data
                .doOnNext {
                    if (!it.containsKey(id) && !consumed) {
                        consumed = true
                        update(id) { create!!.invoke() }.subscribeOn(Schedulers.io()).subscribe()
                    }
                }
                .flatMapSingle { data ->
                    return@flatMapSingle if (!data.containsKey(id) && !consumed) {
                        consumed = true
                        update(id) { create!!.invoke() }.map { data }
                    } else {
                        Single.just(data)
                    }
                }
                .filter { it.containsKey(id) }
                .map { it[id] }
    }

    fun update(id: Task.Id, action: (Task?) -> Task?): Single<Opt<Task>> = hotData
            .updateRx {
                val mutMap = it.toMutableMap()
                val oldTask = mutMap.remove(id)
                val newTask = action.invoke(oldTask)
                if (newTask != null) {
                    mutMap[newTask.taskId] = newTask
                }
                mutMap.toMap()
            }
            .map { Opt(it.newValue[id]) }

    fun remove(id: Task.Id): Single<Opt<Task>> = Single.just(id)
            .flatMap { id ->
                hotData.data
                        .firstOrError()
                        .flatMap { preDeleteMap ->
                            update(id) { null }.map { Opt(preDeleteMap[id]) }
                        }
            }

    fun save(id: Task.Id): Single<Task> = remove(id)
            .map {
                if (it.isNull) throw IllegalArgumentException("Can't find ID to save: $id")
                it.value
            }
            .flatMap { toSave ->
                return@flatMap taskRepo.put(toSave).map { toSave }
            }

    fun load(id: Task.Id): Single<Task> = taskRepo.get(id)
            .map { optTask ->
                if (!optTask.isNull) optTask.value
                else throw IllegalArgumentException("Trying to load unknown task: $id")
            }
            .flatMap { task -> update(id) { task }.map { task } }

    fun startEditor(taskId: Task.Id = Task.Id()) {
        val intent = Intent(context, TaskEditorActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putTaskId(taskId)
        context.startActivity(intent)
    }

    companion object {
        val TAG = App.logTag("Task", "Builder")
    }
}