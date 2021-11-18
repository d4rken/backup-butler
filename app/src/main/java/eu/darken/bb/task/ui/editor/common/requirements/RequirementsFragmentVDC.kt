package eu.darken.bb.task.ui.editor.common.requirements

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.common.requirements.Requirement
import eu.darken.bb.task.core.common.requirements.RequirementsManager
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class RequirementsFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    private val reqMan: RequirementsManager,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private val navArgs by handle.navArgs<RequirementsFragmentArgs>()
    private val taskId: Task.Id = navArgs.taskId

    private val builderData = taskBuilder.task(taskId)

    private val reqChecKTrigger = MutableStateFlow(Unit)
    private val requirementObs = combine(builderData, reqChecKTrigger) { data, _ -> data }
        .map {
            log { "Generating requirements" }
            reqMan.reqsFor(it.taskType, taskId)
        }

    val state = combine(builderData, requirementObs) { data, reqs ->
        State(
            requirements = reqs.filter { !it.satisfied },
            taskType = data.taskType
        )
    }.asLiveData2()

    val runTimePermissionEvent = SingleLiveEvent<Requirement.Permission>()

    fun runMainAction(requirement: Requirement) {
        when (requirement.type) {
            Requirement.Type.PERMISSION -> {
                runTimePermissionEvent.postValue(requirement as Requirement.Permission?)
            }
            Requirement.Type.SAF_ACCESS -> TODO()
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (!granted) return
        reqChecKTrigger.value = Unit
    }

    fun onContinue() = launch {
        val data = builderData.first()

        when (data.taskType) {
            Task.Type.BACKUP_SIMPLE -> RequirementsFragmentDirections
                .actionPermissionFragmentToIntroFragment(taskId = navArgs.taskId)
            Task.Type.RESTORE_SIMPLE -> RequirementsFragmentDirections
                .actionPermissionFragmentToRestoreSourcesFragment(taskId = navArgs.taskId)
        }.navVia(this@RequirementsFragmentVDC)
    }

    data class State(
        val taskType: Task.Type,
        val requirements: List<Requirement> = emptyList(),
    )
}