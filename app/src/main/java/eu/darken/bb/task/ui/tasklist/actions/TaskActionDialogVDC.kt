package eu.darken.bb.task.ui.tasklist.actions

import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.subscribeNullable
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.ui.editor.common.intro.IntroFragmentVDC
import eu.darken.bb.task.ui.tasklist.actions.TaskAction.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class TaskActionDialogVDC @AssistedInject constructor(
    private val taskRepo: TaskRepo,
    private val taskBuilder: TaskBuilder,
    private val processorControl: ProcessorControl,
    @Assisted private val handle: SavedStateHandle,
    @Assisted private val taskId: Task.Id
) : SmartVDC() {

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

    @AssistedFactory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): TaskActionDialogVDC
    }
}