package eu.darken.bb.task.ui.editor.backup

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.ui.editor.backup.destinations.DestinationsFragment
import eu.darken.bb.task.ui.editor.backup.intro.IntroFragment
import eu.darken.bb.task.ui.editor.backup.sources.SourcesFragment
import eu.darken.bb.task.ui.editor.restore.config.RestoreConfigFragment
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KClass


class BackupTaskActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: Task.Id,
        private val taskBuilder: TaskBuilder,
        private val taskRepo: TaskRepo
) : SmartVDC() {
    private val taskObs = taskBuilder.task(taskId)
            .subscribeOn(Schedulers.io())

    private val editorObs = taskObs
            .filter { it.editor != null }
            .map { it.editor!! }
            .doOnNext { editor ->
                stater.update {
                    it.copy(
                            existingTask = editor.isExistingTask()
                    )
                }
            }

    private val taskCheckerObs = editorObs
            .flatMap { it.config }
            .doOnNext { task ->
                stater.update { it.copy(saveable = isTaskComplete(task)) }
            }

    private val stater: Stater<State> = Stater {
        val data = taskObs.blockingFirst()
        val steps = when (data.taskType) {
            Task.Type.BACKUP_SIMPLE -> listOf(State.Step.INTRO, State.Step.SOURCES, State.Step.DESTINATIONS)
            Task.Type.RESTORE_SIMPLE -> TODO()
        }
        State(steps = steps, taskId = taskId, taskType = data.taskType)
    }
            .addLiveDep { taskCheckerObs.subscribe() }

    val state = stater.liveData

    val finishActivity = SingleLiveEvent<Boolean>()

    private fun changeStep(change: Int) {
        stater.update { old ->
            var newPos = old.stepPos + change
            if (newPos < 0) {
                newPos = 0
            } else if (newPos >= old.steps.size) {
                newPos = old.steps.size - 1
            }
            return@update old.copy(
                    stepPos = newPos,
                    allowNext = newPos < old.steps.size - 1,
                    allowPrevious = newPos > 0
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
                    stater.update {
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
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    stater.update {
                        it.copy(allowNext = false, allowPrevious = false)
                    }
                }
                .subscribe { _ ->
                    finishActivity.postValue(true)
                }
    }

    fun previous() {
        if (stater.snapshot.allowPrevious) {
            changeStep(-1)
        } else {
            dismiss()
        }
    }

    fun next() {
        if (stater.snapshot.allowNext) {
            changeStep(+1)
        } else {
            saveTask()
        }
    }

    data class State(
            val taskId: Task.Id,
            val taskType: Task.Type,
            val steps: List<Step>,
            val stepPos: Int = 0,
            val allowPrevious: Boolean = false,
            val allowNext: Boolean = true,
            val saveable: Boolean = false,
            val existingTask: Boolean = false
    ) {
        enum class Step(
                val fragmentClass: KClass<out Fragment>
        ) {
            INTRO(IntroFragment::class),
            SOURCES(SourcesFragment::class),
            DESTINATIONS(DestinationsFragment::class),
            RESTORE_OPTIONS(RestoreConfigFragment::class)
        }
    }


    @AssistedInject.Factory
    interface Factory : VDCFactory<BackupTaskActivityVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): BackupTaskActivityVDC
    }
}