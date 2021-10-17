package eu.darken.bb.storage.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.main.ui.advanced.AdvancedModeFragmentDirections
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.StorageManager
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class StorageListFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val storageManager: StorageManager,
    private val storageBuilder: StorageBuilder,
    processorControl: ProcessorControl
) : SmartVDC() {

    val storageData = storageManager.infos()
        .observeOn(Schedulers.computation())
        .map { infos ->
            StorageState(
                storages = infos.toList(),
                isLoading = false
            )
        }
        .startWithItem(StorageState())
        .asLiveData()

    val navEvents = SingleLiveEvent<NavDirections>()

    val processorEvent = SingleLiveEvent<Boolean>()

    init {
        processorControl.progressHost
            .subscribe { processorEvent.postValue(it.isNotNull) }
            .withScopeVDC(this)
    }

    fun createStorage() {
        storageBuilder.createEditor()
            .observeOn(Schedulers.computation())
            .subscribe { data ->
                AdvancedModeFragmentDirections.actionAdvancedModeFragmentToStorageEditor(
                    storageId = null
                ).run { navEvents.postValue(this) }
            }
    }

    fun editStorage(item: Storage.InfoOpt) {
        log(TAG) { "editStorage($item)" }
        // TODO why does this not  start from the actions dialog?
        AdvancedModeFragmentDirections.actionAdvancedModeFragmentToStorageActionDialog(item.storageId)
            .run { navEvents.postValue(this) }
    }

    data class StorageState(
        val storages: List<Storage.InfoOpt> = emptyList(),
        val isLoading: Boolean = true
    )

    companion object {
        val TAG = logTag("Storage", "StorageList", "VDC")
    }
}