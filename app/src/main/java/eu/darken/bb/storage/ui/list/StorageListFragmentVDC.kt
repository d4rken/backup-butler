package eu.darken.bb.storage.ui.list

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.withStater
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

    private val stater: Stater<State> = Stater(State())
    val state = stater.liveData

    val editTaskEvent = SingleLiveEvent<Storage.Id>()

    init {
        storageManager.infos()
                .subscribeOn(Schedulers.io())
                .doOnNext { infos ->
                    stater.update { state ->
                        state.copy(
                                storages = infos.map { StorageInfoOpt(it) },
                                isLoading = false
                        )
                    }
                }
                .withStater(stater)
    }

    fun createStorage() {
        storageBuilder.startEditor()
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    fun editStorage(item: StorageInfoOpt) {
        Timber.tag(TAG).d("editStorage(%s)", item)
        editTaskEvent.postValue(item.storageId)
    }

    data class State(
            val storages: List<StorageInfoOpt> = emptyList(),
            val isLoading: Boolean = true
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<StorageListFragmentVDC>

    companion object {
        val TAG = App.logTag("Storage", "StorageList", "VDC")
    }
}