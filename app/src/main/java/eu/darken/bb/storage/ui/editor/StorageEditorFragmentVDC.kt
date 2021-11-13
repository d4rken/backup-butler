package eu.darken.bb.storage.ui.editor

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class StorageEditorFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val storageBuilder: StorageBuilder,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

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

    private val storageFlow = flow {
        val storage = storageBuilder.load(storageId) ?: storageBuilder.getEditor(storageId)
        emit(storage)
    }.flatMapLatest { storageBuilder.storage(it.storageId) }

    init {
        storageFlow
            .map { data ->
                log(TAG) { "Navigating to ${data.storageType}" }
                when (data.storageType) {
                    Storage.Type.LOCAL -> StorageEditorFragmentDirections
                        .actionStorageEditorFragmentToLocalEditorFragment(storageId = data.storageId)
                    Storage.Type.SAF -> StorageEditorFragmentDirections
                        .actionStorageEditorFragmentToSafEditorFragment(storageId = data.storageId)
                    null -> StorageEditorFragmentDirections
                        .actionStorageEditorFragmentToTypeSelectionFragment(storageId = data.storageId)
                }
            }
            .onEach { it.navVia(this) }
            .launchInViewModel()
    }

    companion object {
        private val TAG = logTag("Storage", "Editor", "VDC")
    }
}