package eu.darken.bb.task.ui.editor.common.requirements

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.common.requirements.Requirement
import eu.darken.bb.task.core.common.requirements.RequirementsManager
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class RequirementsFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    private val reqMan: RequirementsManager
) : SmartVDC() {

    private val navArgs by handle.navArgs<RequirementsFragmentArgs>()
    private val taskId: Task.Id = navArgs.taskId
    private val builderData = taskBuilder.task(taskId).subscribeOn(Schedulers.io())

    val stater = Stater { State() }
    val state = stater.liveData
    val navEvents = SingleLiveEvent<NavDirections>()
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

    fun onContinue() {
        val nextStep = when (stater.snapshot.taskType) {
            Task.Type.BACKUP_SIMPLE -> RequirementsFragmentDirections.actionPermissionFragmentToIntroFragment(
                taskId = navArgs.taskId
            )
            Task.Type.RESTORE_SIMPLE -> RequirementsFragmentDirections.actionPermissionFragmentToRestoreSourcesFragment(
                taskId = navArgs.taskId
            )
        }
        navEvents.postValue(nextStep)
    }

    data class State(
        val requirements: List<Requirement> = emptyList(),
        val taskType: Task.Type = Task.Type.BACKUP_SIMPLE
    )
}