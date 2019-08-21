package eu.darken.bb.task.ui.tasklist.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.Stater
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.ui.editor.backup.intro.IntroFragmentVDC
import eu.darken.bb.task.ui.tasklist.actions.TaskAction.*
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
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
                .subscribe { maybeTask ->
                    val task = maybeTask.value
                    stateUpdater.update {
                        if (task == null) {
                            it.copy(loading = true, finished = true)
                        } else {
                            it.copy(
                                    taskName = task.taskName,
                                    taskType = task.taskType,
                                    loading = false,
                                    allowedActions = values().toList()
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
                        .map { it.notNullValue() }
                        .delay(200, TimeUnit.MILLISECONDS)
                        .doFinally { stateUpdater.update { it.copy(loading = false, finished = true) } }
                        .subscribe { task ->
                            processorControl.submit(task)
                        }
            }
            EDIT -> {
                taskBuilder.load(taskId)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stateUpdater.update { it.copy(loading = true) } }
                        .delay(200, TimeUnit.MILLISECONDS)
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
            val allowedActions: List<TaskAction> = listOf()
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): TaskActionDialogVDC
    }
}