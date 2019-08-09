package eu.darken.bb.storage.ui.list

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.StorageManager
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class StorageListFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val storageManager: StorageManager,
        private val storageBuilder: StorageBuilder
) : SmartVDC() {

    private val storageInfoObs = storageManager.infos().subscribeOn(Schedulers.io())
            .doOnNext { infos ->
                stateUpdater.update { state -> state.copy(storages = infos.map { StorageInfoOpt(it) }) }
            }

    private val stateUpdater: StateUpdater<State> = StateUpdater(State())
            .addLiveDep {
                storageInfoObs.subscribe()
            }

    val state = stateUpdater.liveData
    val editTaskEvent = SingleLiveEvent<EditActions>()

    fun createStorage() {
        storageBuilder.startEditor()
    }

    fun editStorage(item: StorageInfoOpt) {
        Timber.tag(TAG).d("editStorage(%s)", item)
        editTaskEvent.postValue(EditActions(
                storageId = item.storageId,
                allowView = true,
                allowEdit = true,
                allowDelete = true
        ))
    }

    data class State(
            val storages: List<StorageInfoOpt> = emptyList()
    )

    data class EditActions(
            val storageId: Storage.Id,
            val allowView: Boolean = false,
            val allowEdit: Boolean = false,
            val allowDelete: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<StorageListFragmentVDC>
}