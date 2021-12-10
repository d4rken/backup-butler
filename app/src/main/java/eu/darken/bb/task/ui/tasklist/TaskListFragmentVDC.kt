package eu.darken.bb.task.ui.tasklist

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.replayingShare
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.main.ui.MainFragmentDirections
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.core.results.TaskResultRepo
import eu.darken.bb.task.ui.editor.TaskEditorArgs
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TaskListFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    taskRepo: TaskRepo,
    processorControl: ProcessorControl,
    private val resultRepo: TaskResultRepo,
    dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private val tasksFlow = taskRepo.tasks
        .map { it.values }
        .map { it.toList() }
        .map { tasks -> tasks.filter { !it.isSingleUse } }
        .replayingShare(vdcScope)


    val state = combine(
        tasksFlow,
        tasksFlow.flatMapLatest { tasks -> resultRepo.getLatestTaskResultGlimse(tasks.map { it.taskId }) }
    ) { tasks, results ->
        ViewState(
            tasks = tasks.map { task ->
                TaskListAdapter.Item(
                    task = task,
                    lastResult = results.find { task.taskId == it.taskId },
                    onClickAction = { editTask(it) }
                )
            }
        )
    }.asLiveData2()

    val processorEvent = SingleLiveEvent<Boolean>()

    init {
        processorControl.progressHost
            .onEach { processorEvent.postValue(it != null) }
            .launchInViewModel()
    }

    fun newTask() {
        MainFragmentDirections.actionMainFragmentToTaskEditor(
            args = TaskEditorArgs(taskType = Task.Type.BACKUP_SIMPLE)
        ).navVia(this)
    }

    fun editTask(taskId: Task.Id) {
        Timber.tag(TAG).d("editTask(%s)", taskId)
        MainFragmentDirections.actionMainFragmentToTaskActionDialog(taskId)
            .navVia(this)
    }

    data class ViewState(
        val tasks: List<TaskListAdapter.Item> = emptyList(),
        val hasRunningTask: Boolean = false
    )

    companion object {
        val TAG = logTag("Task", "TaskList", "VDC")
    }
}