package eu.darken.bb.task.ui.editor.common.requirements

import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.common.requirements.Requirement
import eu.darken.bb.task.core.common.requirements.RequirementsManager
import io.reactivex.rxjava3.schedulers.Schedulers

class RequirementsFragmentVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle,
    @Assisted private val taskId: Task.Id,
    private val taskBuilder: TaskBuilder,
    private val reqMan: RequirementsManager
) : SmartVDC() {

    private val builderData = taskBuilder.task(taskId).subscribeOn(Schedulers.io())

    val stater = Stater(State())
    val state = stater.liveData

    val runTimePermissionEvent = SingleLiveEvent<Requirement.Permission>()

    init {
        updateRequirements()
    }

    fun runMainAction(requirement: Requirement) {
        when (requirement.type) {
            Requirement.Type.PERMISSION -> {
                runTimePermissionEvent.postValue(requirement as Requirement.Permission?)
            }
            Requirement.Type.SAF_ACCESS -> TODO()
        }
    }

    private fun updateRequirements() {
        builderData
            .switchMapSingle { reqMan.reqsFor(it.taskType, taskId) }
            .map { reqs -> reqs.filterNot { it.satisfied } }
            .flatMapSingle { newReqs -> stater.updateRx { it.copy(requirements = newReqs) } }
            .subscribe()
            .withScopeVDC(this)
    }

    fun onPermissionResult(granted: Boolean) {
        if (!granted) return
        updateRequirements()
    }

    data class State(
        val requirements: List<Requirement> = emptyList(),
        val taskType: Task.Type = Task.Type.BACKUP_SIMPLE
    )

    @AssistedFactory
    interface Factory : VDCFactory<RequirementsFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): RequirementsFragmentVDC
    }
}