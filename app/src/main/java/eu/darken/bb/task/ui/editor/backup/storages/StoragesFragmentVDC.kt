package eu.darken.bb.task.ui.editor.backup.storages

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.flow.takeUntilAfter
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.list.StorageAdapter
import eu.darken.bb.storage.ui.picker.StoragePickerResult
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class StoragesFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    private val storageManager: StorageManager,
    private val processorControl: ProcessorControl,
    dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {
    private val navArgs by handle.navArgs<StoragesFragmentArgs>()
    private val taskId: Task.Id = navArgs.taskId

    private val editorFlow = taskBuilder.task(taskId)
        .filterNotNull()
        .map { it.editor as SimpleBackupTaskEditor }

    private val stater = DynamicStateFlow(TAG, vdcScope) { State() }
    val state = stater.asLiveData2()

    init {
        editorFlow
            .flatMapConcat { it.editorData }
            .flatMapLatest { dests ->
                storageManager.infos(dests.destinations)
                    .takeUntilAfter { ios -> ios.all { it.isFinished } }
            }
            .map { infos -> infos.map { StorageAdapter.Item(it) } }
            .onEach { storageStatuses ->
                stater.updateBlocking { copy(destinations = storageStatuses.toList()) }
            }
            .launchInViewModel()
    }

    fun removeDestination(storage: StorageAdapter.Item) = launch {
        editorFlow.first().removeStorage(storage.info.storageId)
    }

    fun onStoragePicked(result: StoragePickerResult?) = launch {
        if (result == null) return@launch

        editorFlow.first().addStorage(result.storageId)
    }

    fun addStorage() {
        // Result gets returned via fragment result listener, see onStoragePicked
        StoragesFragmentDirections.actionDestinationsFragmentToStoragePicker(
            taskId = taskId
        ).run { navEvents.postValue(this) }
    }

    fun onNext() {
        StoragesFragmentDirections.actionDestinationsFragmentToSummaryFragment(taskId)
            .navVia(this)
    }

    data class State(
        val destinations: List<StorageAdapter.Item> = emptyList(),
        val isWorking: Boolean = false
    )

    companion object {
        val TAG = logTag("Task", "Editor", "Storages", "VDC")
    }
}