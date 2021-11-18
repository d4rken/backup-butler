package eu.darken.bb.storage.ui.editor.types

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class StorageTypeFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val builder: StorageBuilder,
    dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private val navArgs by handle.navArgs<StorageTypeFragmentArgs>()
    private val storageId: Storage.Id = navArgs.storageId

    val state = flow { emit(builder.getSupportedStorageTypes()) }
        .map { types ->
            State(
                supportedTypes = types.toList()
            )
        }
        .asLiveData2()

    fun createType(type: Storage.Type) = launch {
        val newData = builder.update(storageId) { it!!.copy(storageType = type, editor = null) }
        when (newData!!.storageType) {
            Storage.Type.LOCAL -> StorageTypeFragmentDirections.actionTypeSelectionFragmentToLocalEditorFragment(
                storageId = storageId
            )
            Storage.Type.SAF -> StorageTypeFragmentDirections.actionTypeSelectionFragmentToSafEditorFragment(
                storageId = storageId
            )
            null -> throw IllegalArgumentException("Storage type was null: $newData")
        }.navVia(this@StorageTypeFragmentVDC)
    }

    data class State(
        val supportedTypes: List<Storage.Type>,
        val isWorking: Boolean = false
    )
}