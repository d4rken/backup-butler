package eu.darken.bb.storage.ui.picker

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class StoragePickerFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    private val storageBuilder: StorageBuilder,
    storageManager: StorageManager
) : SmartVDC() {
    private val navArgs by handle.navArgs<StoragePickerFragmentArgs>()
    private val taskId: Task.Id = navArgs.taskId

    private val editorObs = taskBuilder.task(taskId)
        .observeOn(Schedulers.computation())
        .filter { it.editor != null }
        .map { it.editor as SimpleBackupTaskEditor }
    private val editorData = editorObs.flatMap { it.editorData }
    val navEvents = SingleLiveEvent<NavDirections>()

    val storageData = storageManager.infos()
        .observeOn(Schedulers.computation())
        .takeUntil { optInfos -> optInfos.all { it.isFinished } }
        .flatMap { all ->
            editorData
                .map { it.destinations }.map { alreadyAdded ->
                    return@map all.filter { !alreadyAdded.contains(it.storageId) }
                }
                .map { infos ->
                    StorageState(
                        storages = infos,
                        allExistingAdded = infos.isEmpty() && all.isNotEmpty(),
                        isLoading = false
                    )
                }
        }
        .startWithItem(StorageState())
        .asLiveData()

    val finishEvent = SingleLiveEvent<StoragePickerResult?>()

    fun createStorage() {
        storageBuilder.createEditor()
            .observeOn(Schedulers.computation())
            .subscribe { data ->
                // TODO use fragment result api
                StoragePickerFragmentDirections.actionStoragePickerFragmentToStorageEditor()
                    .run { navEvents.postValue(this) }
            }
    }

    fun selectStorage(item: Storage.InfoOpt) {
        StoragePickerResult(
            storageId = item.storageId
        ).run { finishEvent.postValue(this) }
    }

    data class StorageState(
        val storages: List<Storage.InfoOpt> = emptyList(),
        val allExistingAdded: Boolean = false,
        val isLoading: Boolean = true
    )

    companion object {
        val TAG = logTag("Task", "Editor", "Destinations", "Picker", "VDC")
    }
}