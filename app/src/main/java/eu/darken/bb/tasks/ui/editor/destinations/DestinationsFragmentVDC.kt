package eu.darken.bb.tasks.ui.editor.destinations

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.storage.core.StorageInfo
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.tasks.core.DefaultBackupTask
import eu.darken.bb.tasks.core.TaskBuilder
import io.reactivex.schedulers.Schedulers
import java.util.*

class DestinationsFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: UUID,
        private val taskBuilder: TaskBuilder,
        private val storageManager: StorageManager
) : VDC() {

    private val taskObs = taskBuilder.task(taskId)
            .doOnNext { task ->
                val storageStatuses = task.destinations.map { storageManager.info(it).blockingFirst() }
                stateUpdater.update { it.copy(destinations = storageStatuses) }
            }

    private val stateUpdater: StateUpdater<State> = StateUpdater(State())
            .addLiveDep { taskObs.subscribe() }

    val state = stateUpdater.state

    val storagePickerEvent = SingleLiveEvent<List<StorageInfo>>()

    data class State(
            val destinations: List<StorageInfo> = emptyList()
    )

    fun showDestinationPicker() {
        storageManager.infos()
                .subscribeOn(Schedulers.io())
                .flatMap { allStorages ->
                    taskObs.map { it.destinations }.map { alreadyAddedStorages ->
                        return@map allStorages.filter { !alreadyAddedStorages.contains(it.ref) }
                    }
                }
                .firstOrError()
                .subscribe { infos ->
                    storagePickerEvent.postValue(infos.toList())
                }
    }

    fun addDestination(storage: StorageInfo) {
        taskBuilder
                .update(taskId) {
                    it as DefaultBackupTask
                    it.copy(
                            destinations = it.destinations.toMutableSet().apply { add(storage.ref) }.toSet()
                    )
                }
                .subscribeOn(Schedulers.computation())
                .subscribe()
    }

    fun removeDestination(storageInfo: StorageInfo) {
        taskBuilder
                .update(taskId) { task ->
                    task as DefaultBackupTask
                    task.copy(
                            destinations = task.destinations
                                    .toMutableSet()
                                    .filterNot { it.storageId == storageInfo.ref.storageId }
                                    .toSet()
                    )
                }
                .subscribeOn(Schedulers.computation())
                .subscribe()
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<DestinationsFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: UUID): DestinationsFragmentVDC
    }
}