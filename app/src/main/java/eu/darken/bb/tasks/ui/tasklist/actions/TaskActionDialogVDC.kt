package eu.darken.bb.tasks.ui.tasklist.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.tasks.core.BackupTaskRepo
import eu.darken.bb.tasks.core.TaskBuilder
import eu.darken.bb.tasks.ui.editor.intro.IntroFragmentVDC
import eu.darken.bb.tasks.ui.tasklist.TaskActions
import eu.darken.bb.tasks.ui.tasklist.TaskActions.*
import io.reactivex.schedulers.Schedulers
import java.util.*

class TaskActionDialogVDC @AssistedInject constructor(
        private val taskRepo: BackupTaskRepo,
        private val taskBuilder: TaskBuilder,
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: UUID
) : SmartVDC() {

    private val stateUpdater = StateUpdater(State(loading = true))
    val state = stateUpdater.state

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
                                    loading = false,
                                    allowedActions = TaskActions.values().toList()
                            )
                        }
                    }
                }
    }

    fun taskAction(action: TaskActions) {
        when (action) {
            RUN -> TODO()
            EDIT -> {
                taskBuilder.load(taskId)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stateUpdater.update { it.copy(loading = true) } }
                        .subscribe { task ->
                            // TODO launch edit activity
                            taskBuilder.startEditor(task.taskId)
                            stateUpdater.update { it.copy(loading = false, finished = true) }
                        }
            }
            DELETE -> {
                taskRepo.remove(taskId)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stateUpdater.update { it.copy(loading = true) } }
                        .subscribe { _ ->
                            stateUpdater.update { it.copy(loading = false, finished = true) }
                        }
            }
        }
    }

    data class State(
            val loading: Boolean = false,
            val finished: Boolean = false,
            val taskName: String = "",
            val allowedActions: List<TaskActions> = listOf()
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: UUID): TaskActionDialogVDC
    }
}