package eu.darken.bb.task.ui.editor.backup.destinations.picker

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import io.reactivex.rxjava3.schedulers.Schedulers

class StoragePickerFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: Task.Id,
        private val taskBuilder: TaskBuilder,
        private val storageBuilder: StorageBuilder,
        storageManager: StorageManager
) : SmartVDC() {
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
                .firstOrError()
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

    @AssistedInject.Factory
    interface Factory : VDCFactory<StoragePickerFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): StoragePickerFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Task", "Editor", "Destinations", "Picker", "VDC")
    }
}