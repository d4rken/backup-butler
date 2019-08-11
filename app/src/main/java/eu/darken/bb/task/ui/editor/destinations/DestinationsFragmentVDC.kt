package eu.darken.bb.task.ui.editor.destinations

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.list.StorageInfoOpt
import eu.darken.bb.task.core.DefaultTask
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class DestinationsFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: Task.Id,
        private val taskBuilder: TaskBuilder,
        private val storageManager: StorageManager
) : VDC() {

    private val taskObs = taskBuilder.task(taskId)
    private val destinationUpdater = taskObs
            .doOnNext { task ->
                val storageStatuses = task.destinations.map { id ->
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
                    it as DefaultTask
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
                    task as DefaultTask
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
        fun create(handle: SavedStateHandle, taskId: Task.Id): DestinationsFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Task", "Editor", "Destinations", "VDC")
    }
}