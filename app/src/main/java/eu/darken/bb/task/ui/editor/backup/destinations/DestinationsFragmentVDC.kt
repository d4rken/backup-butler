package eu.darken.bb.task.ui.editor.backup.destinations

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.list.StorageInfoOpt
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import io.reactivex.schedulers.Schedulers

class DestinationsFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: Task.Id,
        private val taskBuilder: TaskBuilder,
        private val storageManager: StorageManager
) : SmartVDC() {

    private val editorObs = taskBuilder.task(taskId)
            .filter { it.editor != null }
            .map { it.editor as SimpleBackupTaskEditor }

    private val editor: SimpleBackupTaskEditor by lazy {
        editorObs.blockingFirst()
    }

    private val stater: Stater<State> = Stater(State())
    val state = stater.liveData

    val storagePickerEvent = SingleLiveEvent<List<StorageInfoOpt>>()

    init {
        editorObs
                .switchMap { it.config }
                .switchMap { storageManager.infos(it.destinations) }
                .subscribe { storageStatuses ->
                    stater.update { it.copy(destinations = storageStatuses.toList()) }
                }
                .withScopeVDC(this)
    }

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
                .take(1)
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