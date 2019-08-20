package eu.darken.bb.task.ui.tasklist

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.Opt
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.core.results.TaskResultRepo
import io.reactivex.Observable
import timber.log.Timber

class TaskListFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val taskRepo: TaskRepo,
        private val taskBuilder: TaskBuilder,
        private val processorControl: ProcessorControl,
        private val resultRepo: TaskResultRepo
) : SmartVDC() {

    private val processHostObs: Observable<Opt<Progress.Host>> = processorControl.progressHost
            .doOnNext { optHost ->
                stater.update { it.copy(hasRunningTask = optHost.isNotNull) }
            }

    private val tasksObs: Observable<List<Task>> = taskRepo.tasks.map { it.values }
            .map { it.toList() }
            .doOnNext { tasks ->
                stater.update {
                    it.copy(tasks = tasks.map { task -> TaskState(task = task) })
                }
            }

    private val resultObs: Observable<List<Task.Result>> = tasksObs
            .flatMap { tasks ->
                val ids = tasks.map { it.taskId }
                resultRepo.getLatestTaskResults(ids)
            }
            .doOnNext { results ->
                stater.update { state ->
                    val merged = state.tasks.map { s ->
                        s.copy(lastResult = results.find { s.task.taskId == it.taskId })
                    }
                    state.copy(tasks = merged)
                }
            }

    private val stater = Stater(ViewState())
            .addLiveDep {
                processHostObs.subscribe()
                tasksObs.subscribe()
                resultObs.subscribe()
            }
    val state = stater.liveData

    val editTaskEvent = SingleLiveEvent<EditActions>()

    init {

    }

    fun newTask() {
        taskBuilder.startEditor()
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