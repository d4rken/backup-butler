package eu.darken.bb.storage.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class StorageEditorFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val storageBuilder: StorageBuilder
) : SmartVDC() {

    private val navArgs = handle.navArgs<StorageEditorFragmentArgs>()
    private val storageId: Storage.Id = run {
        log(TAG) { "navArgs=${navArgs.value}" }
        val handleKey = "newId"
        when {
            handle.contains(handleKey) -> handle.get<Storage.Id>(handleKey)!!
            navArgs.value.storageId != null -> navArgs.value.storageId!!
            else -> Storage.Id().also {
                // ID was null, we create a new one
                handle.set(handleKey, it)
            }
        }
    }

    private val storageObs = storageBuilder
        .load(storageId)
        .switchIfEmpty(storageBuilder.createEditor(storageId))
        .flatMapObservable { storageBuilder.storage(it.storageId) }
        .observeOn(Schedulers.computation())

    val finishEvent = SingleLiveEvent<StorageEditorResult?>()
    val navEvents = SingleLiveEvent<NavDirections>()

    init {
        storageObs
            .map { data ->
                when (data.storageType) {
                    Storage.Type.LOCAL -> StorageEditorFragmentDirections
                        .actionStorageEditorFragmentToLocalEditorFragment(storageId = data.storageId)
                    Storage.Type.SAF -> StorageEditorFragmentDirections
                        .actionStorageEditorFragmentToSafEditorFragment(storageId = data.storageId)
                    null -> StorageEditorFragmentDirections
                        .actionStorageEditorFragmentToTypeSelectionFragment(storageId = data.storageId)
                }
            }
            .subscribe { navEvents.postValue(it) }
    }

    companion object {
        private val TAG = logTag("Storage", "Editor", "VDC")
    }
}