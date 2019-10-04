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
import eu.darken.bb.task.core.results.TaskResultRepo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import timber.log.Timber

class TaskListFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val taskRepo: TaskRepo,
        private val taskBuilder: TaskBuilder,
        private val processorControl: ProcessorControl,
        private val resultRepo: TaskResultRepo
) : SmartVDC() {
    private val tasksObs = taskRepo.tasks.map { it.values }
            .map { it.toList() }

    private val stater = Stater(ViewState())
    val state = stater.liveData

    val editTaskEvent = SingleLiveEvent<EditActions>()

    init {
        processorControl.progressHost
                .subscribe { optHost ->
                    stater.update { it.copy(hasRunningTask = optHost.isNotNull) }
                }
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
                    resultRepo.getLatestTaskResults(ids)
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
            taskBuilder.startEditor(taskType = Task.Type.BACKUP_SIMPLE).await()
        }
    }

    fun editTask(item: Task) {
        Timber.tag(TAG).d("editTask(%s)", item)
        editTaskEvent.postValue(EditActions(
                taskId = item.taskId
        ))
    }

    data class ViewState(
            val tasks: List<TaskState> = emptyList(),
            val hasRunningTask: Boolean = false
    )

    data class TaskState(
            val task: Task,
            val lastResult: Task.Result? = null
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