package eu.darken.bb.task.core

import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import io.reactivex.rxjava3.core.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskBuilder @Inject constructor(
    private val taskRepo: TaskRepo,
    private val editors: @JvmSuppressWildcards Map<Task.Type, TaskEditor.Factory<out TaskEditor>>
) {

    private val hotData = HotData<Map<Task.Id, Data>>(tag = TAG) { mutableMapOf() }

    fun task(id: Task.Id): Observable<Data> = hotData.data
        .filter { it.containsKey(id) }
        .map { it[id]!! }

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
            it.editor.snapshot()
        }
        .flatMap { task ->
            return@flatMap taskRepo.put(task).map { task }
        }
        .doOnSuccess { Timber.tag(TAG).d("Saved %s: %s", id, it) }
        .doOnError { Timber.tag(TAG).d(it, "Failed to save %s", id) }
        .map { it }

    /**
     * Attempt to load an existing task into an editor
     */
    private fun load(id: Task.Id): Maybe<Data> = taskRepo.get(id)
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

    /**
     * Attempts to load an existing task into an editor,
     * otherwise creates a new editor.
     */
    fun getEditor(
        taskId: Task.Id = Task.Id(),
        type: Task.Type? = null,
        createNew: Boolean = true,
    ): Single<Data> = hotData.latest
        .flatMapMaybe { Maybe.fromCallable<Data> { it[taskId] } }
        .switchIfEmpty(
            load(taskId)
                .doOnSubscribe { Timber.tag(TAG).d("Trying existing task for %s", taskId) }
                .doOnSuccess { Timber.tag(TAG).d("Loaded existing task for %s", taskId) }
                .doOnError { Timber.tag(TAG).e("Failed to load existing task for %s", taskId) }
                .doOnComplete {
                    if (type == null) throw IllegalStateException(
                        "No task found for $taskId! getEditor requires a type, if there is no existing task."
                    )
                }
        )
        .switchIfEmpty(
            update(taskId) {
                requireNotNull(type) { "If load($taskId) fails, a type needs to be specified." }
                val editor = editors.getValue(type).create(taskId)
                Data(
                    taskId = taskId,
                    taskType = type,
                    editor = editor,
                )
            }
                .map { it.value!! }
                .doOnSubscribe { Timber.tag(TAG).d("Creating new editor for %s", taskId) }
                .doOnSuccess { log(TAG) { "Created new editor: $it" } }
        )

    data class Data(
        val taskId: Task.Id,
        val taskType: Task.Type,
        val editor: TaskEditor? = null
    )

    companion object {
        val TAG = logTag("Task", "Builder")
    }
}