package eu.darken.bb.task.ui.tasklist.actions

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.subscribeNullable
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.BackupTaskExtensions
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.TaskRepo
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
    // TODO use safe args?
    private val taskId: Task.Id = handle.get(BackupTaskExtensions.TASKID_KEY)!!
    private val stateUpdater = Stater(State(loading = true))
    val state = stateUpdater.liveData

    init {
        taskRepo.get(taskId)
            .subscribeOn(Schedulers.io())
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
                    .flatMapCompletable { taskBuilder.startEditor(it.taskId) }
                    .subscribe()
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