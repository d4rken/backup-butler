package eu.darken.bb.task.ui.editor.backup.destinations

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.list.StorageInfoOpt
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class DestinationsFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: Task.Id,
        private val taskBuilder: TaskBuilder,
        private val storageManager: StorageManager
) : VDC() {

    private val editorObs = taskBuilder.task(taskId)
            .filter { it.editor != null }
            .map { it.editor as SimpleBackupTaskEditor }

    private val destinationUpdater = editorObs
            .flatMap { it.config }
            .doOnNext { ed ->
                val storageStatuses = ed.destinations.map { id ->
                    try {
                        val status = storageManager.info(id).blockingFirst()
                        StorageInfoOpt(id, status)
                    } catch (e: Exception) {
                        Timber.tag(TAG).w(e, "Failed to get StatusInfo for $id")
                        StorageInfoOpt(id, null)
                    }
                }
                stater.update { it.copy(destinations = storageStatuses) }
            }

    private val editor: SimpleBackupTaskEditor by lazy {
        editorObs.blockingFirst()
    }

    private val stater: Stater<State> = Stater(State())
            .addLiveDep { destinationUpdater.subscribe() }
    val state = stater.liveData

    val storagePickerEvent = SingleLiveEvent<List<StorageInfoOpt>>()

    data class State(
            val destinations: List<StorageInfoOpt> = emptyList()
    )

    fun showDestinationPicker() {
        storageManager.infos()
                .subscribeOn(Schedulers.io())
                .flatMap { allStorages ->
                    editor.config.map { it.destinations }.map { alreadyAddedStorages ->
                        return@map allStorages.filter { !alreadyAddedStorages.contains(it.ref.storageId) }.map { StorageInfoOpt(it) }
                    }
                }
                .firstOrError()
                .subscribe { infos ->
                    storagePickerEvent.postValue(infos.toList())
                }
    }

    fun addDestination(storage: StorageInfoOpt) {
        editor.addDesination(storage.storageId)
    }

    fun removeDestination(storage: StorageInfoOpt) {
        editor.removeDesination(storage.storageId)
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<DestinationsFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): DestinationsFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Task", "Editor", "Destinations", "VDC")
    }
}