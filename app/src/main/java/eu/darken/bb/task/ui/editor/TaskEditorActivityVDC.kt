package eu.darken.bb.task.ui.editor

import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.R
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize


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

    val finishEvent = SingleLiveEvent<Boolean>()

    private val stater: Stater<State> = Stater {
        val data = taskObs.blockingFirst()
        val steps = when (data.taskType) {
            Task.Type.BACKUP_SIMPLE -> StepFlow.BACKUP_SIMPLE
            Task.Type.RESTORE_SIMPLE -> StepFlow.RESTORE_SIMPLE
        }
        State(stepFlow = steps, taskId = taskId, taskType = data.taskType)
    }
    val state = stater.liveData

    init {
        stater.data
                .subscribe { handle.set("state", it) }
                .withScopeVDC(this)

        editorObs
                .flatMap { it.isValid() }
                .subscribe { isValid ->
                    stater.update { it.copy(isValid = isValid) }
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

    fun updateCurrent(@IdRes current: Int) {
        stater.update { it.copy(currentStep = current) }
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

    fun dismiss() {
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


    fun save(execute: Boolean = false) {
        saveTask().subscribe { savedTask ->
            if (execute) processorControl.submit(savedTask)
            finishEvent.postValue(true)
        }
    }

    @Parcelize
    data class State(
            val taskId: Task.Id,
            val taskType: Task.Type,
            val stepFlow: StepFlow,
            @IdRes val currentStep: Int = 0,
            val isValid: Boolean = false,
            val isExistingTask: Boolean = false,
            val isLoading: Boolean = true,
            val isOneTimeTask: Boolean = true
    ) : Parcelable

    enum class StepFlow(@IdRes val start: Int) {
        BACKUP_SIMPLE(R.id.introFragment),
        RESTORE_SIMPLE(R.id.restoreSourcesFragment);
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<TaskEditorActivityVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): TaskEditorActivityVDC
    }
}