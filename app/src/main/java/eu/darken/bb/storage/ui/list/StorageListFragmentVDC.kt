package eu.darken.bb.storage.ui.list

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.main.ui.MainFragmentDirections
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.StorageManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class StorageListFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val storageManager: StorageManager,
    private val storageBuilder: StorageBuilder,
    processorControl: ProcessorControl,
    dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    val storageData = storageManager.infos()
        .map { infos ->
            StorageState(
                storages = infos.map { StorageAdapter.Item(it) },
                isLoading = false
            )
        }
        .asLiveData2()

    val processorEvent = SingleLiveEvent<Boolean>()

    init {
        processorControl.progressHost
            .onEach { processorEvent.postValue(it != null) }
            .launchInViewModel()
    }

    fun createStorage() {
        MainFragmentDirections.actionMainFragmentToStorageEditor(
            storageId = null
        ).run { navEvents.postValue(this) }
    }

    fun editStorage(item: StorageAdapter.Item) {
        log(TAG) { "editStorage($item)" }
        // TODO why does this not  start from the actions dialog?
        MainFragmentDirections.actionMainFragmentToStorageActionDialog(item.info.storageId)
            .run { navEvents.postValue(this) }
    }

    data class StorageState(
        val storages: List<StorageAdapter.Item> = emptyList(),
        val isLoading: Boolean = true
    )

    companion object {
        val TAG = logTag("Storage", "StorageList", "VDC")
    }
}