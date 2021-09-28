package eu.darken.bb.storage.ui.list

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.StorageManager
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StorageListFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val storageManager: StorageManager,
    private val storageBuilder: StorageBuilder,
    processorControl: ProcessorControl
) : SmartVDC() {

    val storageData = storageManager.infos()
        .subscribeOn(Schedulers.io())
        .map { infos ->
            StorageState(
                storages = infos.toList(),
                isLoading = false
            )
        }
        .startWithItem(StorageState())
        .toLiveData()

    val editStorageEvent = SingleLiveEvent<Storage.Id>()

    val processorEvent = SingleLiveEvent<Boolean>()

    init {
        processorControl.progressHost
            .subscribe { processorEvent.postValue(it.isNotNull) }
            .withScopeVDC(this)
    }

    fun createStorage() {
        storageBuilder.startEditor()
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun editStorage(item: Storage.InfoOpt) {
        Timber.tag(TAG).d("editStorage(%s)", item)
        editStorageEvent.postValue(item.storageId)
    }

    data class StorageState(
        val storages: List<Storage.InfoOpt> = emptyList(),
        val isLoading: Boolean = true
    )

    companion object {
        val TAG = logTag("Storage", "StorageList", "VDC")
    }
}