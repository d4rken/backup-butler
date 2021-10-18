package eu.darken.bb.task.ui.editor.common.requirements

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.common.requirements.Requirement
import eu.darken.bb.task.core.common.requirements.RequirementsManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class RequirementsFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    private val reqMan: RequirementsManager
) : SmartVDC() {

    private val navArgs by handle.navArgs<RequirementsFragmentArgs>()
    private val taskId: Task.Id = navArgs.taskId

    private val builderData = taskBuilder.task(taskId)
        .observeOn(Schedulers.computation())

    private val reqChecKTrigger = BehaviorSubject.createDefault(Unit)
    private val requirementObs = Observable
        .combineLatest(builderData, reqChecKTrigger) { data, _ -> data }
        .switchMapSingle {
            log { "Generating requirements" }
            reqMan.reqsFor(it.taskType, taskId)
        }

    val state = Observable.combineLatest(builderData, requirementObs) { data, reqs ->
        State(
            requirements = reqs.filter { !it.satisfied },
            taskType = data.taskType
        )
    }.asLiveData()

    val navEvents = SingleLiveEvent<NavDirections>()
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
        reqChecKTrigger.onNext(Unit)
    }

    fun onContinue() {
        builderData.map {
            when (it.taskType) {
                Task.Type.BACKUP_SIMPLE -> RequirementsFragmentDirections.actionPermissionFragmentToIntroFragment(
                    taskId = navArgs.taskId
                )
                Task.Type.RESTORE_SIMPLE -> RequirementsFragmentDirections.actionPermissionFragmentToRestoreSourcesFragment(
                    taskId = navArgs.taskId
                )
            }
        }.subscribe { navEvents.postValue(it) }
    }

    data class State(
        val taskType: Task.Type,
        val requirements: List<Requirement> = emptyList(),
    )
}