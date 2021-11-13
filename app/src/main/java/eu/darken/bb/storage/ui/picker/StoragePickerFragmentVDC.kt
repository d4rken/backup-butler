package eu.darken.bb.storage.ui.picker

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.takeUntilAfter
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.editor.StorageEditorResult
import eu.darken.bb.storage.ui.list.StorageAdapter
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class StoragePickerFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    storageManager: StorageManager,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {
    private val navArgs by handle.navArgs<StoragePickerFragmentArgs>()
    private val taskId: Task.Id? = navArgs.taskId

    private suspend fun getAlreadyAdded(): Set<Storage.Id> {
        if (taskId == null) return emptySet()

        val data = taskBuilder.task(taskId).first()
        if (data.editor == null) return emptySet()

        data.editor as SimpleBackupTaskEditor

        return data.editor.editorData.first().destinations
    }

    val storageData = storageManager.infos()
        .takeUntilAfter { optInfos -> optInfos.all { it.isFinished } }
        .map { all ->
            val alreadyAdded = getAlreadyAdded()
            val available = all.filter { !alreadyAdded.contains(it.storageId) }

            StorageState(
                storages = available.map { StorageAdapter.Item(it) },
                allExistingAdded = available.isEmpty() && all.isNotEmpty(),
                isLoading = false
            )
        }
        .onStart { emit(StorageState()) }
        .asLiveData2()

    val finishEvent = SingleLiveEvent<StoragePickerResult?>()

    fun createStorage() {
        StoragePickerFragmentDirections.actionStoragePickerFragmentToStorageEditor()
            .run { navEvents.postValue(this) }
    }

    fun selectStorage(item: StorageAdapter.Item) {
        StoragePickerResult(
            storageId = item.info.storageId
        ).run { finishEvent.postValue(this) }
    }

    fun onStorageEditorResult(result: StorageEditorResult) {
        log(TAG) { "onStorageEditorResult(result=$result)" }
    }

    data class StorageState(
        val storages: List<StorageAdapter.Item> = emptyList(),
        val allExistingAdded: Boolean = false,
        val isLoading: Boolean = true
    )

    companion object {
        private val TAG = logTag("Storage", "Picker", "VDC")
    }
}