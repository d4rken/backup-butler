package eu.darken.bb.task.ui.editor.destinations

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.list.StorageInfoOpt
import eu.darken.bb.task.core.BackupTask
import eu.darken.bb.task.core.DefaultBackupTask
import eu.darken.bb.task.core.TaskBuilder
import io.reactivex.schedulers.Schedulers

class DestinationsFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: BackupTask.Id,
        private val taskBuilder: TaskBuilder,
        private val storageManager: StorageManager
) : VDC() {

    private val taskObs = taskBuilder.task(taskId)
    private val destinationUpdater = taskObs
            .doOnNext { task ->
                val storageStatuses = task.destinations.map {
                    val status = storageManager.info(it).blockingFirst().value
                    StorageInfoOpt(it, status)
                }
                stateUpdater.update { it.copy(destinations = storageStatuses) }
            }

    private val stateUpdater: StateUpdater<State> = StateUpdater(State())
            .addLiveDep { destinationUpdater.subscribe() }
    val state = stateUpdater.state

    val storagePickerEvent = SingleLiveEvent<List<StorageInfoOpt>>()

    data class State(
            val destinations: List<StorageInfoOpt> = emptyList()
    )

    fun showDestinationPicker() {
        storageManager.infos()
                .subscribeOn(Schedulers.io())
                .flatMap { allStorages ->
                    taskObs.map { it.destinations }.map { alreadyAddedStorages ->
                        return@map allStorages.filter { !alreadyAddedStorages.contains(it.ref.storageId) }.map { StorageInfoOpt(it) }
                    }
                }
                .firstOrError()
                .subscribe { infos ->
                    storagePickerEvent.postValue(infos.toList())
                }
    }

    fun addDestination(storage: StorageInfoOpt) {
        taskBuilder
                .update(taskId) {
                    it as DefaultBackupTask
                    it.copy(
                            destinations = it.destinations.toMutableSet().apply { add(storage.storageId) }.toSet()
                    )
                }
                .subscribeOn(Schedulers.computation())
                .subscribe()
    }

    fun removeDestination(storage: StorageInfoOpt) {
        taskBuilder
                .update(taskId) { task ->
                    task as DefaultBackupTask
                    task.copy(
                            destinations = task.destinations
                                    .toMutableSet()
                                    .filterNot { it == storage.storageId }
                                    .toSet()
                    )
                }
                .subscribeOn(Schedulers.computation())
                .subscribe()
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<DestinationsFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: BackupTask.Id): DestinationsFragmentVDC
    }
}