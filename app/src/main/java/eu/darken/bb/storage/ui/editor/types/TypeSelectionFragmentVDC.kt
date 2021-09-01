package eu.darken.bb.storage.ui.editor.types

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import io.reactivex.rxjava3.schedulers.Schedulers

class TypeSelectionFragmentVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle,
    @Assisted private val storageId: Storage.Id,
    private val builder: StorageBuilder,
    @AppContext private val context: Context
) : SmartVDC() {

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

    @AssistedFactory
    interface Factory : VDCFactory<TypeSelectionFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): TypeSelectionFragmentVDC
    }
}