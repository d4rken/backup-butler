package eu.darken.bb.tasks.ui.newtask

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
import eu.darken.bb.tasks.core.DefaultBackupTask
import eu.darken.bb.tasks.core.TaskBuilder
import eu.darken.bb.tasks.ui.newtask.destinations.DestinationsFragment
import eu.darken.bb.tasks.ui.newtask.intro.IntroFragment
import eu.darken.bb.tasks.ui.newtask.sources.SourcesFragment
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import kotlin.reflect.KClass


class NewTaskActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: UUID,
        private val taskBuilder: TaskBuilder
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
            .doOnNext { task -> stateUpdater.update { it.copy(creatable = isComplete(task)) } }
            .toLiveData()
    val steps = Observables.combineLatest(stateUpdater.data, taskObs)
            .toLiveData()
    val state = stateUpdater.state
    val finishActivity = SingleLiveEvent<Boolean>()

    init {

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

    private fun isComplete(backupTask: BackupTask?): Boolean {
        if (backupTask == null) return false

        return backupTask.taskName.length > 3
    }

    fun createTask() {
        taskBuilder.store(taskId)
                .doOnSubscribe {
                    stateUpdater.update {
                        it.copy(allowNext = false, allowPrevious = false)
                    }
                }
                .subscribeOn(Schedulers.computation())
                .subscribe {
                    finishActivity.postValue(true)
                }
    }

    fun dismiss() {
        Timber.i("DIMISS")
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
            createTask()
        }
    }

    data class State(
            val step: Step,
            val allowPrevious: Boolean = false,
            val allowNext: Boolean = false,
            val creatable: Boolean = false
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
    interface Factory : VDCFactory<NewTaskActivityVDC> {
        fun create(handle: SavedStateHandle, taskId: UUID): NewTaskActivityVDC
    }
}