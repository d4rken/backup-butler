package eu.darken.bb.tasks.ui.newtaskcreation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.SavedStateVDCFactory

class NewTaskFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle
) : VDC() {

    val state = MutableLiveData<State>(State(emoji = ""))

    data class State(val emoji: String)

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<NewTaskFragmentVDC>
}