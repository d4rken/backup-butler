package eu.darken.bb.storage.ui.viewer.details.page

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class DetailPageFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        @Assisted private val backupSpecId: BackupSpec.Id,
        @Assisted private val backupId: Backup.Id,
        private val storageManager: StorageManager
) : SmartVDC() {

    private val storageObs = storageManager.getStorage(storageId).subscribeOn(Schedulers.io())
    private val contentObs = storageObs.flatMapObservable { it.content() }
    private val itemsObs = storageObs
            .flatMapObservable { storage ->
                contentObs.flatMap { contents ->
                    val content = contents.find { it.backupSpec.specId == backupSpecId }!!
                    storage.details(content, backupId)
                }
            }
            .doOnNext { details ->
                stateUpdater.update { state -> state.copy(items = details.items.toList()) }
            }
            .doOnError {
                finishEvent.postValue(Any())
            }
            .onErrorResumeNext(Observable.empty())

    private val stateUpdater: StateUpdater<State> = StateUpdater(State())
            .addLiveDep {
                itemsObs.subscribe()
            }

    init {
        Timber.tag(TAG).v("StorageId %s, BackupSpecId: %s, BackupId: %s", storageId, backupSpecId, backupId)
    }

    val state = stateUpdater.liveData
    val finishEvent = SingleLiveEvent<Any>()

    data class State(
            val items: List<Backup.Item> = emptyList()
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<DetailPageFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id, backupSpecId: BackupSpec.Id, backupId: Backup.Id): DetailPageFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Storage", "Details", "Page", "VDC")
    }
}