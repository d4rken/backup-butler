package eu.darken.bb.storage.ui.viewer

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import io.reactivex.rxjava3.schedulers.Schedulers


class StorageViewerActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        storageManager: StorageManager
) : SmartVDC() {

    val errorEvent = SingleLiveEvent<Throwable>()
    val finishActivity = SingleLiveEvent<Boolean>()

    private val stater: Stater<State> = Stater(State(storageId = storageId))
    val state = stater.liveData

    init {
        storageManager.infos(listOf(storageId)).subscribeOn(Schedulers.io())
                .map { it.single() }
                .subscribe { optInfo ->
                    stater.update {
                        it.copy(
                                storageId = storageId,
                                storageType = optInfo.info?.storageType,
                                label = optInfo.info?.config?.label ?: ""
                        )
                    }
                    if (optInfo.anyError != null) {
                        errorEvent.postValue(optInfo.anyError)
                        finishActivity.postValue(true)
                    }
                }
                .withScopeVDC(this)
    }

    data class State(
            val storageId: Storage.Id,
            val label: String = "",
            val storageType: Storage.Type? = null,
            val loading: Boolean = true
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<StorageViewerActivityVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageViewerActivityVDC
    }
}