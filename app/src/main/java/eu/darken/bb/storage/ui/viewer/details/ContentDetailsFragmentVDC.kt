package eu.darken.bb.storage.ui.viewer.details

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.core.Versioning
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class ContentDetailsFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        @Assisted private val backupSpecId: BackupSpec.Id,
        private val storageManager: StorageManager
) : SmartVDC() {

    private val storageObs = storageManager.getStorage(storageId).subscribeOn(Schedulers.io())
    private val contentObs = storageObs.flatMapObservable { it.content() }
            .map { contents -> contents.find { it.backupSpec.specId == backupSpecId }!! }
            .doOnNext { item ->
                stateUpdater.update { state ->
                    state.copy(
                            backupSpec = item.backupSpec,
                            versions = item.versioning.versions.sortedBy { it.createdAt }.reversed()
                    )
                }
            }
            .doOnError { error ->
                stateUpdater.update {
                    it.copy(
                            error = error,
                            loading = false
                    )
                }
                finishEvent.postValue(Any())
            }
            .onErrorResumeNext(Observable.empty<Storage.Content>())

    private val stateUpdater: StateUpdater<State> = StateUpdater(State())
            .addLiveDep {
                contentObs.subscribe()
            }
    val state = stateUpdater.liveData
    val finishEvent = SingleLiveEvent<Any>()

    data class State(
            val backupSpec: BackupSpec? = null,
            val versions: List<Versioning.Version> = listOf(),
            val loading: Boolean = true,
            val error: Throwable? = null
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<ContentDetailsFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id, backupSpecId: BackupSpec.Id): ContentDetailsFragmentVDC
    }
}