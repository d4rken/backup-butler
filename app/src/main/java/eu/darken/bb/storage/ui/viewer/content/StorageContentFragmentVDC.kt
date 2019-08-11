package eu.darken.bb.storage.ui.viewer.content

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.Stater
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import io.reactivex.schedulers.Schedulers

class StorageContentFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        private val storageManager: StorageManager
) : SmartVDC() {

    private val storageObs = storageManager.getStorage(storageId).subscribeOn(Schedulers.io())
    private val contentObs = storageObs.flatMapObservable { it.content() }
            .doOnNext { storageContents ->
                stater.update {
                    it.copy(
                            contents = storageContents.toList(),
                            loading = false
                    )
                }
            }
            .doOnError { error ->
                stater.update {
                    it.copy(
                            error = error,
                            loading = false
                    )
                }
                finishEvent.postValue(true)
            }
            .onErrorReturnItem(emptyList())

    private val stater: Stater<State> = Stater(State())
            .addLiveDep {
                contentObs.subscribe()
            }
    val state = stater.liveData

    val finishEvent = SingleLiveEvent<Boolean>()
    val contentActionEvent = SingleLiveEvent<ContentActions>()

    fun viewContent(item: Storage.Content) {
        contentActionEvent.postValue(ContentActions(
                storageId = item.storageId,
                backupSpecId = item.backupSpec.specId,
                allowView = true,
                allowDelete = true
        ))
    }

    data class State(
            val contents: List<Storage.Content> = emptyList(),
            val loading: Boolean = true,
            val error: Throwable? = null
    )

    data class ContentActions(
            val storageId: Storage.Id,
            val backupSpecId: BackupSpec.Id,
            val allowView: Boolean = false,
            val allowDelete: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<StorageContentFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageContentFragmentVDC
    }
}