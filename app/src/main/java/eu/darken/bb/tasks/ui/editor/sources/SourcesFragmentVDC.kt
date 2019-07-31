package eu.darken.bb.tasks.ui.editor.sources

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backups.Backup
import eu.darken.bb.backups.BackupConfig
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.tasks.core.DefaultBackupTask
import eu.darken.bb.tasks.core.TaskBuilder
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import java.util.*

class SourcesFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: UUID,
        private val taskBuilder: TaskBuilder,
        private val storageManager: StorageManager
) : VDC() {
    private val stateUpdater = StateUpdater(State())
    private val taskObs = taskBuilder.task(taskId)
    val state = Observables.combineLatest(stateUpdater.data, taskObs)
            .map { (state, task) ->
                val sources = task.sources.toList()
                state.copy(sources = sources)
            }
            .toLiveData()

    val sourceCreatorEvent = SingleLiveEvent<List<Backup.Type>>()

    data class State(
            val sources: List<BackupConfig> = emptyList()
    )

    fun showSourcePicker() {
//        storageManager.infos()
//                .subscribeOn(Schedulers.io())
//                .flatMap { allStorages ->
//                    taskObs.map { it.destinations }.map { alreadyAddedStorages ->
//                        return@map allStorages.filter { !alreadyAddedStorages.contains(it.ref) }
//                    }
//                }
//                .firstOrError()
//                .subscribe { infos ->
//                    sourceCreatorEvent.postValue(infos.toList())
//                }
    }

    fun addSource(source: BackupConfig) {
        taskBuilder
                .update(taskId) {
                    it as DefaultBackupTask
//                    it.copy(
//                            destinations = it.destinations.toMutableSet().apply { add(storage.ref) }.toSet()
//                    )
                }
                .subscribeOn(Schedulers.computation())
                .subscribe()
    }

    fun removeSource(source: BackupConfig) {
        taskBuilder
                .update(taskId) { task ->
                    task as DefaultBackupTask
//                    task.copy(
//                            sources = task.sources
//                                    .toMutableSet()
//                                    .filterNot { it.storageId == storageInfo.ref.storageId }
//                                    .toSet()
//                    )
                }
                .subscribeOn(Schedulers.computation())
                .subscribe()
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<SourcesFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: UUID): SourcesFragmentVDC
    }
}