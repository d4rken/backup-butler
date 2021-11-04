package eu.darken.bb.task.ui.editor.backup.storages

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
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.list.StorageAdapter
import eu.darken.bb.storage.ui.picker.StoragePickerResult
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class StoragesFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    private val storageManager: StorageManager,
    private val processorControl: ProcessorControl
) : SmartVDC() {
    private val navArgs by handle.navArgs<StoragesFragmentArgs>()
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
            .map { infos -> infos.map { StorageAdapter.Item(it) } }
            .subscribe { storageStatuses ->
                stater.update { it.copy(destinations = storageStatuses.toList()) }
            }
            .withScopeVDC(this)
    }

    fun removeDestination(storage: StorageAdapter.Item) {
        editor.removeStorage(storage.info.storageId)
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
                editor.addStorage(result.storageId)
            }
    }

    fun addStorage() {
        // Result gets returned via fragment result listener, see onStoragePicked
        StoragesFragmentDirections.actionDestinationsFragmentToStoragePicker(
            taskId = taskId
        ).run { navEvents.postValue(this) }
    }

    data class State(
        val destinations: List<StorageAdapter.Item> = emptyList(),
        val isWorking: Boolean = false
    )

    companion object {
        val TAG = logTag("Task", "Editor", "Storages", "VDC")
    }
}