package eu.darken.bb.task.ui.tasklist.actions

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.Stater
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.ui.editor.TaskEditorArgs
import eu.darken.bb.task.ui.tasklist.actions.TaskAction.*
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class TaskActionDialogVDC @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val taskRepo: TaskRepo,
    private val processorControl: ProcessorControl,
) : Smart2VDC(dispatcherProvider) {

    private val navArgs by handle.navArgs<TaskActionDialogArgs>()
    private val taskId: Task.Id = navArgs.taskId
    private val stateUpdater = Stater { State(loading = true) }
    val state = stateUpdater.liveData

    init {
        launch {
            val task = taskRepo.get(taskId)

            val actions = listOf(
                Confirmable(RUN) { taskAction(it) },
                Confirmable(EDIT) { taskAction(it) },
                Confirmable(DELETE, requiredLvl = 1) { taskAction(it) },
            )

            stateUpdater.update {
                if (task == null) {
                    it.copy(loading = true, finished = true)
                } else {
                    it.copy(
                        taskName = task.label,
                        taskType = task.taskType,
                        loading = false,
                        allowedActions = actions
                    )
                }
            }
        }
    }

    fun taskAction(action: TaskAction) {
        stateUpdater.update { it.copy(loading = true) }
        launch {
            try {
                when (action) {
                    RUN -> {
                        val task = taskRepo.get(taskId) ?: return@launch
                        processorControl.submit(task)
                    }
                    EDIT -> {
                        TaskActionDialogDirections.actionTaskActionDialogToTaskEditor(
                            args = TaskEditorArgs(taskId = taskId)
                        ).run { navEvents.postValue(this) }
                    }
                    DELETE -> {
                        delay(200)
                        taskRepo.remove(taskId)
                    }
                }
            } finally {
                stateUpdater.update { it.copy(loading = false, finished = true) }
            }
        }
    }

    data class State(
        val loading: Boolean = false,
        val finished: Boolean = false,
        val taskName: String = "",
        val taskType: Task.Type? = null,
        val allowedActions: List<Confirmable<TaskAction>> = listOf()
    )

}