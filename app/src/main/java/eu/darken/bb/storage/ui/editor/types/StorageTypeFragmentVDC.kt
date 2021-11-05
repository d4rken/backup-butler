package eu.darken.bb.storage.ui.editor.types

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.navigation.NavEventsSource
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.via
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import javax.inject.Inject

@HiltViewModel
class StorageTypeFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val builder: StorageBuilder,
) : SmartVDC(), NavEventsSource {

    private val navArgs by handle.navArgs<StorageTypeFragmentArgs>()
    private val storageId: Storage.Id = navArgs.storageId

    val state = builder.getSupportedStorageTypes()
        .map { types ->
            State(
                supportedTypes = types.toList()
            )
        }
        .asLiveData()

    override val navEvents = SingleLiveEvent<NavDirections>()

    fun createType(type: Storage.Type) {
        builder
            .update(storageId) { it!!.copy(storageType = type, editor = null) }
            .map { it.notNullValue() }
            .subscribe { data ->
                when (data.storageType) {
                    Storage.Type.LOCAL -> StorageTypeFragmentDirections.actionTypeSelectionFragmentToLocalEditorFragment(
                        storageId = storageId
                    )
                    Storage.Type.SAF -> StorageTypeFragmentDirections.actionTypeSelectionFragmentToSafEditorFragment(
                        storageId = storageId
                    )
                    null -> throw IllegalArgumentException("Storage type was null: $data")
                }.via(this)
            }
    }

    data class State(
        val supportedTypes: List<Storage.Type>,
        val isWorking: Boolean = false
    )
}