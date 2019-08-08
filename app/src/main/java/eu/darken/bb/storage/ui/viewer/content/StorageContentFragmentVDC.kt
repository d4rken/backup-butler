package eu.darken.bb.storage.ui.viewer.content

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class StorageContentFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        private val storageManager: StorageManager
) : SmartVDC() {

    private val storageObs = storageManager.getStorage(storageId).subscribeOn(Schedulers.io())
    private val contentObs = storageObs.flatMapObservable { it.content() }
            .doOnNext { storageContents ->
                stateUpdater.update {
                    it.copy(contents = storageContents.toList())
                }
            }
            .doOnError { error ->
                stateUpdater.update {
                    it.copy(
                            error = error,
                            loading = false
                    )
                }
                finishEvent.postValue(true)
            }
            .onErrorReturnItem(emptyList())

    private val stateUpdater: StateUpdater<State> = StateUpdater(State())
            .addLiveDep {
                contentObs.subscribe()
            }
    val finishEvent = SingleLiveEvent<Boolean>()

    val state = stateUpdater.state

    fun viewContent(item: Storage.Content) {
        Timber.tag(TAG).d("viewContent(%s)", item)

    }

    data class State(
            val contents: List<Storage.Content> = emptyList(),
            val loading: Boolean = true,
            val error: Throwable? = null
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<StorageContentFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageContentFragmentVDC
    }
}