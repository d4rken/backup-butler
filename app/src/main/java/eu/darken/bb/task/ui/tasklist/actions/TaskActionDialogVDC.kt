package eu.darken.bb.task.ui.tasklist.actions

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.subscribeNullable
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.ui.editor.TaskEditorArgs
import eu.darken.bb.task.ui.tasklist.actions.TaskAction.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TaskActionDialogVDC @Inject constructor(
    private val taskRepo: TaskRepo,
    private val taskBuilder: TaskBuilder,
    private val processorControl: ProcessorControl,
    handle: SavedStateHandle,
) : SmartVDC() {

    private val navArgs by handle.navArgs<TaskActionDialogArgs>()
    private val taskId: Task.Id = navArgs.taskId
    private val stateUpdater = Stater { State(loading = true) }
    val state = stateUpdater.liveData
    val navEvents = SingleLiveEvent<NavDirections>()

    init {
        taskRepo.get(taskId)
            .observeOn(Schedulers.computation())
            .subscribeNullable { task ->
                val actions = listOf(
                    Confirmable(RUN),
                    Confirmable(EDIT),
                    Confirmable(DELETE, requiredLvl = 1)
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
        when (action) {
            RUN -> {
                taskRepo.get(taskId)
                    .doOnSubscribe { stateUpdater.update { it.copy(loading = true) } }
                    .subscribeOn(Schedulers.io())
                    .doFinally { stateUpdater.update { it.copy(loading = false, finished = true) } }
                    .subscribe { task ->
                        processorControl.submit(task)
                    }
            }
            EDIT -> {
                taskBuilder.load(taskId)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { stateUpdater.update { it.copy(loading = true) } }
                    .doFinally { stateUpdater.update { it.copy(loading = false, finished = true) } }
                    .subscribe {
                        TaskActionDialogDirections.actionTaskActionDialogToTaskEditor(
                            args = TaskEditorArgs(taskId = it.taskId, taskType = Task.Type.BACKUP_SIMPLE)
                        ).run { navEvents.postValue(this) }
                    }
            }
            DELETE -> {
                Single.timer(200, TimeUnit.MILLISECONDS)
                    .flatMap { taskRepo.remove(taskId) }
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { stateUpdater.update { it.copy(loading = true) } }
                    .doFinally { stateUpdater.update { it.copy(loading = false, finished = true) } }
                    .subscribe()
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