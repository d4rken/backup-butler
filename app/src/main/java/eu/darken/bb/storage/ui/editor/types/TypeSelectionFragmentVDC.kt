package eu.darken.bb.storage.ui.editor.types

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import io.reactivex.schedulers.Schedulers

class TypeSelectionFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        private val builder: StorageBuilder,
        @AppContext private val context: Context
) : SmartVDC(), BaseEditorFragment.VDC {

    override val state = builder.getSupportedStorageTypes()
            .map { types ->
                State(
                        supportedTypes = types.toList()
                )
            }
            .toLiveData()
    override val finishActivityEvent: SingleLiveEvent<Any> = SingleLiveEvent()

    fun createType(type: Storage.Type) {
        builder.update(storageId) { it!!.copy(storageType = type) }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    override fun onNavigateBack(): Boolean {
        builder.remove(storageId)
                .subscribeOn(Schedulers.io())
                .subscribe { _ ->
                    finishActivityEvent.postValue(true)
                }
        return true
    }

    data class State(
            val supportedTypes: List<Storage.Type>,
            val isWorking: Boolean = false,
            override val isExisting: Boolean = false
    ) : BaseEditorFragment.VDC.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<TypeSelectionFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): TypeSelectionFragmentVDC
    }
}