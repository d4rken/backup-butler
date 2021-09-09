package eu.darken.bb.common.files.ui.picker.types

import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.App
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.ui.picker.APathPicker
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.storage.core.Storage
import javax.inject.Inject

@HiltViewModel
class TypesPickerFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
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

    companion object {
        val TAG = App.logTag("Picker", "Types", "VDC")
    }
}