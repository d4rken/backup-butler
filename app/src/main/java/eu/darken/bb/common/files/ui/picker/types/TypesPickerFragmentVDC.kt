package eu.darken.bb.common.files.ui.picker.types

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.ui.picker.APathPicker
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage

class TypesPickerFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val options: APathPicker.Options
) : SmartVDC() {

    private val stater = Stater {
        val types = options.allowedTypes.map {
            when (it) {
                APath.PathType.RAW -> throw UnsupportedOperationException("$it is not supported")
                APath.PathType.LOCAL -> Storage.Type.LOCAL
                APath.PathType.SAF -> Storage.Type.SAF
            }
        }
        State(allowedTypes = types.sortedBy { it.name }.toList())
    }
    val state = stater.liveData

    val typeEvents = SingleLiveEvent<Storage.Type>()

    fun selectType(type: Storage.Type) {
        typeEvents.postValue(type)
    }

    data class State(
            val allowedTypes: List<Storage.Type>
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<TypesPickerFragmentVDC> {
        fun create(handle: SavedStateHandle, options: APathPicker.Options): TypesPickerFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Picker", "Types", "VDC")
    }
}