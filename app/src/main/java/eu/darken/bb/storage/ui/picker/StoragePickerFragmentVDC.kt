package eu.darken.bb.storage.ui.picker

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.editor.StorageEditorResult
import eu.darken.bb.storage.ui.list.StorageAdapter
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class StoragePickerFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    storageManager: StorageManager
) : SmartVDC() {
    private val navArgs by handle.navArgs<StoragePickerFragmentArgs>()
    private val taskId: Task.Id? = navArgs.taskId

    val navEvents = SingleLiveEvent<NavDirections>()

    private val alreadyAddedObs = (taskId?.let { Observable.just(it) } ?: Observable.empty())
        .observeOn(Schedulers.computation())
        .flatMap { taskBuilder.task(it) }
        .filter { it.editor != null }
        .map { it.editor as SimpleBackupTaskEditor }
        .flatMap { it.editorData }
        .map { it.destinations }
        .switchIfEmpty(Observable.just(emptySet()))

    val storageData = storageManager.infos()
        .observeOn(Schedulers.computation())
        .takeUntil { optInfos -> optInfos.all { it.isFinished } }
        .flatMap { all ->
            alreadyAddedObs
                .map { alreadyAdded ->
                    return@map all.filter { !alreadyAdded.contains(it.storageId) }
                }
                .map { infos ->
                    StorageState(
                        storages = infos.map { StorageAdapter.Item(it) },
                        allExistingAdded = infos.isEmpty() && all.isNotEmpty(),
                        isLoading = false
                    )
                }
        }
        .startWithItem(StorageState())
        .asLiveData()

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