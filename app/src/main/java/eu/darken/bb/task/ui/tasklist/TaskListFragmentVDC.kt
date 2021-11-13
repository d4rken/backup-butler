package eu.darken.bb.task.ui.tasklist

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.smart.SmartVDC
import eu.darken.bb.main.ui.MainFragmentDirections
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.core.results.TaskResultRepo
import eu.darken.bb.task.ui.editor.TaskEditorArgs
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TaskListFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val taskRepo: TaskRepo,
    private val taskBuilder: TaskBuilder,
    processorControl: ProcessorControl,
    private val resultRepo: TaskResultRepo,
    private val dispatcherProvider: DispatcherProvider,
) : SmartVDC(dispatcherProvider) {

    private val tasksFlow = taskRepo.tasks
        .map { it.values }
        .map { it.toList() }
        .map { tasks -> tasks.filter { !it.isOneTimeUse } }

    private val stater = DynamicStateFlow(TAG, vdcScope) { ViewState() }
    val state = stater.asLiveData2()

    val editTaskEvent = SingleLiveEvent<EditActions>()
    val processorEvent = SingleLiveEvent<Boolean>()
    val navEvents = SingleLiveEvent<NavDirections>()

    init {
        processorControl.progressHost
            .onEach { processorEvent.postValue(it != null) }
            .launchInViewModel()

        tasksFlow
            .onEach { tasks ->
                stater.updateBlocking {
                    copy(tasks = tasks.map { task -> TaskListAdapter.Item(task = task) })
                }
            }
            .launchInViewModel()
        tasksFlow
            .flatMapConcat { tasks ->
                val ids = tasks.map { it.taskId }
                resultRepo.getLatestTaskResultGlimse(ids)
            }
            .onEach { results ->
                stater.updateBlocking {
                    val merged = this.tasks.map { s ->
                        s.copy(lastResult = results.find { s.task.taskId == it.taskId })
                    }
                    copy(tasks = merged)
                }
            }
            .launchInViewModel()
    }

    fun newTask() {
        MainFragmentDirections.actionMainFragmentToTaskEditor(
            args = TaskEditorArgs(taskType = Task.Type.BACKUP_SIMPLE)
        ).run { navEvents.postValue(this) }
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
        val tasks: List<TaskListAdapter.Item> = emptyList(),
        val hasRunningTask: Boolean = false
    )

    data class EditActions(
        val taskId: Task.Id
    )

    companion object {
        val TAG = logTag("Task", "TaskList", "VDC")
    }
}