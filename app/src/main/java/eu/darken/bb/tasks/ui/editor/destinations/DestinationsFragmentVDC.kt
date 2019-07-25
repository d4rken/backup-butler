package eu.darken.bb.tasks.ui.editor.destinations

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.storage.core.StorageRefRepo
import java.util.*

class DestinationsFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: UUID,
        private val storageRefRepo: StorageRefRepo
) : VDC() {

    val state = MutableLiveData<State>(State(emoji = ""))

    data class State(val emoji: String)

    @AssistedInject.Factory
    interface Factory : VDCFactory<DestinationsFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: UUID): DestinationsFragmentVDC
    }
}