package eu.darken.bb.task.ui.editor

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.task.core.BackupTaskRepo
import eu.darken.bb.task.core.DefaultTask
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.ui.editor.destinations.DestinationsFragment
import eu.darken.bb.task.ui.editor.intro.IntroFragment
import eu.darken.bb.task.ui.editor.sources.SourcesFragment
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KClass


class TaskEditorActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: Task.Id,
        private val taskBuilder: TaskBuilder,
        private val taskRepo: BackupTaskRepo
) : SmartVDC() {

    private val taskObs = taskBuilder
            .task(taskId) {
                DefaultTask(
                        taskName = "",
                        taskId = taskId,
                        sources = setOf(),
                        destinations = setOf()
                )
            }
            .doOnNext { task ->
                stateUpdater.update {
                    it.copy(saveable = isTaskComplete(task))
                }
            }

    private val stateUpdater: StateUpdater<State> = StateUpdater(
            startValue = State(step = State.Step.INTRO, allowNext = true, taskId = taskId)
    )
            .addLiveDep { taskObs.subscribe() }
    val state = stateUpdater.liveData

    val finishActivity = SingleLiveEvent<Boolean>()

    init {
        taskRepo.get(taskId)
                .subscribeOn(Schedulers.io())
                .subscribe { optTask ->
                    stateUpdater.update { it.copy(existingTask = optTask.isNotNull) }
                }
    }

    private fun changeStep(dir: Int) {
        stateUpdater.update { old ->
            val new = State.Step.values().find { it.stepPos == old.step.stepPos + dir }
            val allowNext = State.Step.values().find { new != null && it.stepPos == new.stepPos + 1 } != null
            val allowPrevious = State.Step.values().find { new != null && it.stepPos == new.stepPos - 1 } != null
            return@update old.copy(
                    step = new ?: old.step,
                    allowNext = allowNext,
                    allowPrevious = allowPrevious
            )
        }
    }

    private fun isTaskComplete(task: Task?): Boolean {
        if (task == null) return false
        task.taskName.isNotBlank()
        return true
    }

    private fun saveTask() {
        taskBuilder.save(taskId)
                .doOnSubscribe {
                    stateUpdater.update {
                        it.copy(allowNext = false, allowPrevious = false)
                    }
                }
                .subscribeOn(Schedulers.computation())
                .subscribe { savedTask ->
                    finishActivity.postValue(true)
                }
    }

    private fun dismiss() {
        taskBuilder.remove(taskId)
        finishActivity.call()
    }

    fun previous() {
        if (stateUpdater.snapshot.allowPrevious) {
            changeStep(-1)
        } else {
            dismiss()
        }
    }

    fun next() {
        if (stateUpdater.snapshot.allowNext) {
            changeStep(+1)
        } else {
            saveTask()
        }
    }

    data class State(
            val step: Step,
            val allowPrevious: Boolean = false,
            val allowNext: Boolean = false,
            val saveable: Boolean = false,
            val existingTask: Boolean = false,
            val taskId: Task.Id
    ) {
        enum class Step(
                val stepPos: Int,
                val fragmentClass: KClass<out Fragment>
        ) {
            INTRO(0, IntroFragment::class),
            SOURCES(1, SourcesFragment::class),
            DESTINATIONS(2, DestinationsFragment::class)
        }
    }


    @AssistedInject.Factory
    interface Factory : VDCFactory<TaskEditorActivityVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): TaskEditorActivityVDC
    }
}