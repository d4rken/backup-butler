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
import io.reactivex.Observable
import timber.log.Timber

class TaskListFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val taskRepo: TaskRepo,
        private val taskBuilder: TaskBuilder,
        private val processorControl: ProcessorControl
) : SmartVDC() {

    private val processHostObs: Observable<Opt<Progress.Host>> = processorControl.progressHost
            .doOnNext { optHost ->
                stater.update { it.copy(hasRunningTask = optHost.isNotNull) }
            }

    private val taskRepoObs: Observable<List<Task>> = taskRepo.tasks.map { it.values }
            .map { it.toList() }
            .doOnNext { tasks ->
                stater.update { it.copy(repos = tasks) }
            }


    private val stater = Stater(ViewState())
            .addLiveDep {
                processHostObs.subscribe()
                taskRepoObs.subscribe()
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
                taskId = item.taskId,
                allowEdit = true,
                allowDelete = true
        ))
    }

    data class ViewState(
            val repos: List<Task> = emptyList(),
            val hasRunningTask: Boolean = false
    )

    data class EditActions(
            val taskId: Task.Id,
            val allowEdit: Boolean = false,
            val allowDelete: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<TaskListFragmentVDC>


    companion object {
        val TAG = App.logTag("Task", "TaskList", "VDC")
    }
}