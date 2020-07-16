package eu.darken.bb.task.ui.editor

import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.annotation.Keep
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.R
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.swallowInterruptExceptions
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.common.requirements.RequirementsManager
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize


class TaskEditorActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: Task.Id,
        private val taskBuilder: TaskBuilder,
        private val reqMan: RequirementsManager
) : SmartVDC() {
    private val taskObs = taskBuilder.task(taskId)
            .subscribeOn(Schedulers.io())

    private val editorObs = taskObs
            .filter { it.editor != null }
            .map { it.editor!! }

    private val editorDataObs = editorObs.flatMap { it.editorData }

    val finishEvent = SingleLiveEvent<Boolean>()

    private val stater: Stater<State> = Stater {
        val data = taskObs.blockingFirst()
        val isReqReady = reqMan.reqsFor(data.taskType)
                .map { reqs -> reqs.all { it.satisfied } }
                .blockingGet()
        val steps = when (data.taskType) {
            Task.Type.BACKUP_SIMPLE -> StepFlow.BACKUP_SIMPLE
            Task.Type.RESTORE_SIMPLE -> {
                val restoreData = editorDataObs.blockingFirst() as SimpleRestoreTaskEditor.Data
                if (restoreData.backupTargets.size == 1) {
                    StepFlow.RESTORE_SIMPLE_SINGLE
                } else {
                    StepFlow.RESTORE_SIMPLE
                }
            }
        }
        State(stepFlow = steps, taskId = taskId, taskType = data.taskType, requiresSetup = !isReqReady)
    }
    val state = stater.liveData

    init {
        taskObs
                .switchMapSingle { reqMan.reqsFor(it.taskType) }
                .map { reqs -> reqs.all { it.satisfied } }
                .subscribe { isReady ->
                    stater.update { it.copy(requiresSetup = !isReady) }
                }
                .withScopeVDC(this)

        stater.data
                .subscribe { handle.set("state", it) }
                .withScopeVDC(this)

        editorObs
                .flatMap { it.isValid() }
                .swallowInterruptExceptions()
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


    @Keep @Parcelize
    data class State(
            val taskId: Task.Id,
            val taskType: Task.Type,
            val stepFlow: StepFlow,
            val isValid: Boolean = false,
            val isExistingTask: Boolean = false,
            val isLoading: Boolean = true,
            val isOneTimeTask: Boolean = true,
            val requiresSetup: Boolean = true
    ) : Parcelable

    enum class StepFlow(@IdRes val start: Int) {
        BACKUP_SIMPLE(R.id.introFragment),
        RESTORE_SIMPLE(R.id.restoreSourcesFragment),
        RESTORE_SIMPLE_SINGLE(R.id.restoreConfigFragment);
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<TaskEditorActivityVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): TaskEditorActivityVDC
    }
}