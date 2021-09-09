package eu.darken.bb.task.core

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.App
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.task.ui.editor.TaskEditorActivity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskRepo: TaskRepo,
    private val editors: @JvmSuppressWildcards Map<Task.Type, TaskEditor.Factory<out TaskEditor>>
) {

    private val hotData = HotData<Map<Task.Id, Data>>(mutableMapOf())

    init {
        hotData.data
            .observeOn(Schedulers.computation())
            .subscribe { dataMap ->
                dataMap.entries.forEach { (uuid, data) ->
                    if (data.editor == null) {
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
        .doOnSuccess { Timber.tag(TAG).v("Task updated: %s (%s): %s", id, action, it) }

    fun remove(id: Task.Id): Single<Opt<Data>> = Single.just(id)
        .flatMap {
            hotData.latest
                .flatMap { preDeleteMap ->
                    update(id) { null }.map { Opt(preDeleteMap[id]) }
                }
        }
        .doOnSuccess { Timber.tag(TAG).v("Removed task: %s", id) }

    fun save(id: Task.Id): Single<Task> = remove(id)
        .doOnSubscribe { Timber.tag(TAG).d("Saving %s", id) }
        .map {
            it.notNullValue("Can't find ID to save: $id")
        }
        .flatMap {
            checkNotNull(it.editor) { "Can't save builder data NULL editor: $it" }
            it.editor.save()
        }
        .flatMap { task ->
            return@flatMap taskRepo.put(task).map { task }
        }
        .doOnSuccess { Timber.tag(TAG).d("Saved %s: %s", id, it) }
        .doOnError { Timber.tag(TAG).d(it, "Failed to save %s", id) }
        .map { it }

    fun load(id: Task.Id): Maybe<Data> = taskRepo.get(id)
        .doOnSuccess { Timber.tag(TAG).d("Next: %s", it) }
        .flatMapSingle { task ->
            val editor = editors.getValue(task.taskType).create(task.taskId)
            editor.load(task).blockingAwait()
            val data = Data(
                taskId = task.taskId,
                taskType = task.taskType,
                editor = editor
            )
            update(id) { data }.map { data }
        }
        .doOnSuccess { Timber.tag(TAG).d("Loaded %s: %s", id, it) }
        .doOnError { Timber.tag(TAG).e(it, "Failed to load %s", id) }
        .doOnComplete { Timber.tag(TAG).d("No task found for %s", id) }

    fun startEditor(taskId: Task.Id): Completable = hotData.latest
        .flatMapMaybe { Maybe.fromCallable<Data> { it[taskId] } }
        .switchIfEmpty(
            load(taskId)
                .doOnSubscribe { Timber.tag(TAG).d("Trying existing task for %s", taskId) }
                .doOnSuccess { Timber.tag(TAG).d("Loaded existing task for %s", taskId) }
                .doOnError { Timber.tag(TAG).e("Failed to load existing task for %s", taskId) }
        )
        .doOnSuccess { data ->
            Timber.tag(TAG).v("Starting editor for ID %s", taskId)
            val intent = Intent(context, TaskEditorActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putTaskId(data.taskId)
            context.startActivity(intent)
        }
        .ignoreElement()

    fun createEditor(newId: Task.Id = Task.Id(), type: Task.Type): Single<Data> = hotData.latest
        .map { existingData ->
            require(!existingData.containsKey(newId)) { "Builder with this ID already exists: $newId" }
            Data(taskId = newId, taskType = type, editor = editors.getValue(type).create(newId))
        }
        .flatMap { data -> update(data.taskId) { data }.map { data } }

    data class Data(
        val taskId: Task.Id,
        val taskType: Task.Type,
        val editor: TaskEditor? = null
    )

    companion object {
        val TAG = App.logTag("Task", "Builder")
    }
}