package eu.darken.bb.task.ui.tasklist

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.core.results.TaskResult
import eu.darken.bb.task.core.results.TaskResultRepo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class TaskListFragmentVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle,
    private val taskRepo: TaskRepo,
    private val taskBuilder: TaskBuilder,
    processorControl: ProcessorControl,
    private val resultRepo: TaskResultRepo
) : SmartVDC() {
    private val tasksObs = taskRepo.tasks.map { it.values }
        .map { it.toList() }
        .map { tasks -> tasks.filter { !it.isOneTimeTask } }

    private val stater = Stater(ViewState())
    val state = stater.liveData

    val editTaskEvent = SingleLiveEvent<EditActions>()

    val processorEvent = SingleLiveEvent<Boolean>()

    init {
        processorControl.progressHost
            .subscribe { processorEvent.postValue(it.isNotNull) }
            .withScopeVDC(this)

        tasksObs
            .subscribe { tasks ->
                stater.update {
                    it.copy(tasks = tasks.map { task -> TaskState(task = task) })
                }
            }
            .withScopeVDC(this)
        tasksObs
            .flatMap { tasks ->
                val ids = tasks.map { it.taskId }
                resultRepo.getLatestTaskResultGlimse(ids)
            }
            .subscribe { results ->
                stater.update { state ->
                    val merged = state.tasks.map { s ->
                        s.copy(lastResult = results.find { s.task.taskId == it.taskId })
                    }
                    state.copy(tasks = merged)
                }
            }
            .withScopeVDC(this)
    }

    fun newTask() {
        GlobalScope.launch {
            taskBuilder.createEditor(type = Task.Type.BACKUP_SIMPLE)
                .flatMapCompletable { taskBuilder.startEditor(it.taskId) }
                .blockingAwait()
        }
    }

    fun editTask(item: Task) {
        Timber.tag(TAG).d("editTask(%s)", item)
        editTaskEvent.postValue(
            EditActions(
                taskId = item.taskId
            )
        )
    }

    data class ViewState(
        val tasks: List<TaskState> = emptyList(),
        val hasRunningTask: Boolean = false
    )

    data class TaskState(
        val task: Task,
        val lastResult: TaskResult? = null
    )

    data class EditActions(
        val taskId: Task.Id
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<TaskListFragmentVDC>


    companion object {
        val TAG = App.logTag("Task", "TaskList", "VDC")
    }
}