package eu.darken.bb.task.ui.editor

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.ui.editor.backup.destinations.DestinationsFragment
import eu.darken.bb.task.ui.editor.backup.intro.IntroFragment
import eu.darken.bb.task.ui.editor.backup.sources.SourcesFragment
import eu.darken.bb.task.ui.editor.restore.config.RestoreConfigFragment
import eu.darken.bb.task.ui.editor.restore.sources.RestoreSourcesFragment
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize
import kotlin.reflect.KClass


class TaskEditorActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: Task.Id,
        private val taskBuilder: TaskBuilder,
        private val processorControl: ProcessorControl
) : SmartVDC() {
    private val taskObs = taskBuilder.task(taskId)
            .subscribeOn(Schedulers.io())

    private val editorObs = taskObs
            .filter { it.editor != null }
            .map { it.editor!! }

    val stepEvent = SingleLiveEvent<Pair<State.Step, Task.Id>>()

    val finishEvent = SingleLiveEvent<Boolean>()

    private val stater: Stater<State> = Stater {
        val state = handle.get<State>("state")
        return@Stater if (state?.taskId == taskId) {
            state
        } else {
            val data = taskObs.blockingFirst()
            val steps = when (data.taskType) {
                Task.Type.BACKUP_SIMPLE -> listOf(State.Step.BACKUP_INTRO, State.Step.BACKUP_SOURCES, State.Step.BACKUP_DESTINATIONS)
                Task.Type.RESTORE_SIMPLE -> listOf(State.Step.RESTORE_SOURCES, State.Step.RESTORE_OPTIONS)
            }
            stepEvent.postValue(Pair(steps[0], taskId))
            State(steps = steps, taskId = taskId, taskType = data.taskType)
        }
    }
    val state = stater.liveData

    init {
        stater.data.subscribe {
            handle.set("state", it)
        }

        editorObs
                .flatMap { it.isValidTask() }
                .subscribe { isValid ->
                    stater.update { it.copy(isComplete = isValid) }
                }
                .withScopeVDC(this)

        editorObs
                .flatMap { it.editorData }
                .subscribe { data ->
                    stater.update {
                        it.copy(
                                isExistingTask = data.isExistingTask,
                                isLoading = false,
                                isOneTimeTask = data.isOneTimeTask
                        )
                    }
                }
                .withScopeVDC(this)
    }

    private fun changeStep(change: Int) {
        stater.update { old ->
            var newPos = old.stepPos + change
            if (newPos < 0) {
                newPos = 0
            } else if (newPos >= old.steps.size) {
                newPos = old.steps.size - 1
            }
            stepEvent.postValue(Pair(old.steps[newPos], taskId))
            return@update old.copy(
                    stepPos = newPos
            )
        }
    }

    private fun saveTask(): Single<Task> {
        return taskBuilder.save(taskId)
                .doOnSubscribe {
                    stater.update {
                        it.copy(isLoading = true)
                    }
                }
                .subscribeOn(Schedulers.computation())
    }

    private fun dismiss() {
        taskBuilder.remove(taskId)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    stater.update {
                        it.copy(isLoading = true)
                    }
                }
                .subscribe { _ ->
                    finishEvent.postValue(true)
                }
    }

    fun previous() {
        if (stater.snapshot.stepPos == 0) {
            cancel()
        } else {
            changeStep(-1)
        }
    }

    fun next() {
        changeStep(+1)
    }

    fun cancel() {
        dismiss()
    }

    fun save() {
        saveTask().subscribe { savedTask ->
            finishEvent.postValue(true)
        }
    }

    fun execute() {
        saveTask()
                .subscribe { savedTask ->
                    processorControl.submit(savedTask)
                    finishEvent.postValue(true)
                }
    }

    @Parcelize
    data class State(
            val taskId: Task.Id,
            val taskType: Task.Type,
            val steps: List<Step>,
            val stepPos: Int = 0,
            val isComplete: Boolean = false,
            val isExistingTask: Boolean = false,
            val isLoading: Boolean = true,
            val isOneTimeTask: Boolean = true
    ) : Parcelable {
        enum class Step(
                val fragmentClass: KClass<out Fragment>
        ) {
            BACKUP_INTRO(IntroFragment::class),
            BACKUP_SOURCES(SourcesFragment::class),
            BACKUP_DESTINATIONS(DestinationsFragment::class),
            RESTORE_SOURCES(RestoreSourcesFragment::class),
            RESTORE_OPTIONS(RestoreConfigFragment::class)
        }
    }


    @AssistedInject.Factory
    interface Factory : VDCFactory<TaskEditorActivityVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): TaskEditorActivityVDC
    }
}