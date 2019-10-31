package eu.darken.bb.storage.ui.list

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC
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

    val storageData = storageManager.infos()
            .subscribeOn(Schedulers.io())
            .map { infos ->
                StorageState(
                        storages = infos.map { Storage.InfoOpt(it) },
                        isLoading = false)
            }
            .startWith(StorageState())
            .toLiveData()

    val editTaskEvent = SingleLiveEvent<Storage.Id>()

    fun createStorage() {
        storageBuilder.startEditor()
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    fun editStorage(item: Storage.InfoOpt) {
        Timber.tag(TAG).d("editStorage(%s)", item)
        editTaskEvent.postValue(item.storageId)
    }

    data class StorageState(
            val storages: List<Storage.InfoOpt> = emptyList(),
            val isLoading: Boolean = true
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<StorageListFragmentVDC>

    companion object {
        val TAG = App.logTag("Storage", "StorageList", "VDC")
    }
}