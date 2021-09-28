package eu.darken.bb.task.ui.editor.backup.destinations.picker

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.latest
import eu.darken.bb.common.rx.toLiveData
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
        .subscribeOn(Schedulers.io())
        .filter { it.editor != null }
        .map { it.editor as SimpleBackupTaskEditor }
    private val editorData = editorObs.flatMap { it.editorData }

    val storageData = storageManager.infos()
        .subscribeOn(Schedulers.io())
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
        .toLiveData()

    val finishEvent = SingleLiveEvent<Any>()

    fun createStorage() {
        storageBuilder.startEditor()
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun selectStorage(item: Storage.InfoOpt) {
        taskBuilder.task(taskId).subscribeOn(Schedulers.io())
            .latest()
            .map { it.editor as SimpleBackupTaskEditor }
            .subscribe { editor ->
                editor.addDestination(item.storageId)
            }
        finishEvent.postValue(Any())
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