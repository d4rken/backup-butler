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
import timber.log.Timber
import javax.inject.Inject

@PerApp
class TaskBuilder @Inject constructor(
        @AppContext private val context: Context,
        private val taskRepo: TaskRepo,
        private val editors: @JvmSuppressWildcards Map<Task.Type, TaskEditor.Factory<out TaskEditor>>
) {

    private val hotData = HotData<Map<Task.Id, Data>>(mutableMapOf())

    init {
        hotData.data
                .observeOn(Schedulers.computation())
                .subscribe { dataMap ->
                    dataMap.entries.forEach { (uuid, data) ->
                        if (data.taskType != null && data.editor == null) {
                            val editor = editors.getValue(data.taskType).create(uuid)
                            update(uuid) { it!!.copy(editor = editor) }.blockingGet()
                        }
                    }
                }
    }

    fun task(id: Task.Id): Observable<Data> {
        return hotData.data
                .filter { it.containsKey(id) }
                .map { it[id] }
    }

    fun update(id: Task.Id, action: (Data?) -> Data?): Single<Opt<Data>> = hotData
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

    fun remove(id: Task.Id): Single<Opt<Data>> = Single.just(id)
            .flatMap { id ->
                hotData.data
                        .firstOrError()
                        .flatMap { preDeleteMap ->
                            update(id) { null }.map { Opt(preDeleteMap[id]) }
                        }
            }

    fun save(id: Task.Id): Single<Task> = remove(id)
            .doOnSubscribe { Timber.tag(TAG).d("Saving %s", id) }
            .map {
                if (it.isNull) throw IllegalArgumentException("Can't find ID to save: $id")
                it.value
            }
            .flatMap {
                if (it.editor == null) throw IllegalStateException("Can't save builder data NULL editor: $it")
                it.editor.save()
            }
            .flatMap { task ->
                return@flatMap taskRepo.put(task).map { task }
            }
            .doOnSuccess { Timber.tag(TAG).d("Saved %s: %s", id, it) }
            .doOnError { Timber.tag(TAG).d(it, "Failed to save %s", id) }
            .map { it }

    fun load(id: Task.Id): Single<Data> = taskRepo.get(id)
            .map { optTask ->
                if (!optTask.isNull) optTask.value
                else throw IllegalArgumentException("Trying to load unknown task: $id")
            }
            .flatMap { task ->
                val editor = editors.getValue(task.taskType).create(task.taskId)
                editor.load(task).blockingGet()
                val data = Data(
                        taskId = task.taskId,
                        taskType = task.taskType,
                        editor = editor
                )
                update(id) { data }.map { data }
            }

    fun startEditor(taskId: Task.Id = Task.Id()) {
        load(taskId)
                .onErrorResumeNext {
                    Timber.tag(TAG).d("No existing task for id %s, creating new dataset.", taskId)
                    update(taskId) { Data(taskId = taskId) }.map { it.value!! }
                }
                .subscribe { data ->
                    Timber.tag(TAG).v("Starting editor for ID %s", taskId)
                    val intent = Intent(context, TaskEditorActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putTaskId(data.taskId)
                    context.startActivity(intent)
                }
    }

    data class Data(
            val taskId: Task.Id,
            val taskType: Task.Type? = null,
            val editor: TaskEditor? = null
    )

    companion object {
        val TAG = App.logTag("Task", "Builder")
    }
}