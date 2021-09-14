package eu.darken.bb.storage.ui.editor.types

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class TypeSelectionFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val builder: StorageBuilder,
) : SmartVDC() {
    private val navArgs by handle.navArgs<TypeSelectionFragmentArgs>()
    private val storageId: Storage.Id = navArgs.storageId

    val state = builder.getSupportedStorageTypes()
        .map { types ->
            State(
                supportedTypes = types.toList()
            )
        }
        .toLiveData()

    val navigationEvent = SingleLiveEvent<Pair<Storage.Type, Storage.Id>>()

    fun createType(type: Storage.Type) {
        builder.update(storageId) { it!!.copy(storageType = type) }
            .subscribeOn(Schedulers.io())
            .doFinally { navigationEvent.postValue(type to storageId) }
            .subscribe()
    }

    data class State(
        val supportedTypes: List<Storage.Type>,
        val isWorking: Boolean = false
    )
}