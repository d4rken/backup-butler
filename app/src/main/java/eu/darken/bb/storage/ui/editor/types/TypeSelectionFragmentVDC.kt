package eu.darken.bb.storage.ui.editor.types

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class TypeSelectionFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    @Assisted private val storageId: Storage.Id,
    private val builder: StorageBuilder,
    @ApplicationContext private val context: Context
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
}