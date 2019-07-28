package eu.darken.bb.tasks.ui.editor

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.tasks.core.BackupTask
import eu.darken.bb.tasks.core.BackupTaskRepo
import eu.darken.bb.tasks.core.DefaultBackupTask
import eu.darken.bb.tasks.core.TaskBuilder
import eu.darken.bb.tasks.ui.editor.destinations.DestinationsFragment
import eu.darken.bb.tasks.ui.editor.intro.IntroFragment
import eu.darken.bb.tasks.ui.editor.sources.SourcesFragment
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.reflect.KClass


class TaskEditorActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: UUID,
        private val taskBuilder: TaskBuilder,
        private val taskRepo: BackupTaskRepo
) : SmartVDC() {

    private val taskObs = taskBuilder.task(taskId) {
        DefaultBackupTask(
                taskName = "",
                taskId = taskId,
                sources = listOf(),
                destinations = listOf()
        )
    }
    private val stateUpdater = StateUpdater(startValue = State(
            step = State.Step.INTRO,
            allowNext = true
    ))
    val task = taskObs
            .doOnNext { task -> stateUpdater.update { it.copy(saveable = isTaskComplete(task)) } }
            .toLiveData()
    val steps = Observables.combineLatest(stateUpdater.data, taskObs)
            .toLiveData()
    val state = stateUpdater.state
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

    private fun isTaskComplete(backupTask: BackupTask?): Boolean {
        if (backupTask == null) return false

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
        if (stateUpdater.snapshot?.allowPrevious == true) {
            changeStep(-1)
        } else {
            dismiss()
        }
    }

    fun next() {
        if (stateUpdater.snapshot?.allowNext == true) {
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
            val existingTask: Boolean = false
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
        fun create(handle: SavedStateHandle, taskId: UUID): TaskEditorActivityVDC
    }
}