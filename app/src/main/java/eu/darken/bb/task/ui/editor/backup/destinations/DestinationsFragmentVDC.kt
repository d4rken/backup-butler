package eu.darken.bb.task.ui.editor.backup.destinations

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.latest
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.picker.StoragePickerResult
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class DestinationsFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    private val storageManager: StorageManager,
    private val processorControl: ProcessorControl
) : SmartVDC() {
    private val navArgs by handle.navArgs<DestinationsFragmentArgs>()
    private val taskId: Task.Id = navArgs.taskId

    private val editorObs = taskBuilder.task(taskId)
        .filter { it.editor != null }
        .map { it.editor as SimpleBackupTaskEditor }

    private val editor: SimpleBackupTaskEditor by lazy {
        editorObs.blockingFirst()
    }

    private val editorData = editorObs.flatMap { it.editorData }

    private val stater: Stater<State> = Stater { State() }
    val state = stater.liveData
    val navEvents = SingleLiveEvent<NavDirections>()
    val finishEvent = SingleLiveEvent<Any>()

    init {
        editorData
            .switchMap { dests ->
                storageManager.infos(dests.destinations)
                    .takeUntil { ios -> ios.all { it.isFinished } }
            }
            .subscribe { storageStatuses ->
                stater.update { it.copy(destinations = storageStatuses.toList()) }
            }
            .withScopeVDC(this)
    }

    fun removeDestination(storage: Storage.InfoOpt) {
        editor.removeDestination(storage.storageId)
    }

    fun executeTask() {
        save(true)
    }

    fun saveTask() {
        save(false)
    }

    private fun save(execute: Boolean = false) {
        taskBuilder.save(taskId)
            .observeOn(Schedulers.computation())
            .doOnSubscribe {
                stater.update {
                    it.copy(isWorking = true)
                }
            }
            .subscribe { savedTask ->
                if (execute) processorControl.submit(savedTask)
                finishEvent.postValue(true)
            }
    }

    fun onStoragePicked(result: StoragePickerResult?) {
        if (result == null) return

        taskBuilder.task(taskId)
            .observeOn(Schedulers.computation())
            .latest()
            .map { it.editor as SimpleBackupTaskEditor }
            .subscribe { editor ->
                editor.addDestination(result.storageId)
            }
    }

    fun addStorage() {
        // Result gets returned via fragment result listener, see onStoragePicked
        DestinationsFragmentDirections.actionDestinationsFragmentToStoragePicker(
            taskId = taskId
        ).run { navEvents.postValue(this) }
    }

    data class State(
        val destinations: List<Storage.InfoOpt> = emptyList(),
        val isWorking: Boolean = false
    )

    companion object {
        val TAG = logTag("Task", "Editor", "Destinations", "VDC")
    }
}