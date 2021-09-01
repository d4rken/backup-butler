package eu.darken.bb.main.ui.advanced

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.VDC


class AdvancedActivityVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle
) : VDC() {

    val state: MutableLiveData<State> = MutableLiveData(State(false))

    fun onGo() {
        state.postValue(state.value!!.copy(ready = true))
    }

    data class State(val ready: Boolean)

    @AssistedFactory
    interface Factory : SavedStateVDCFactory<AdvancedActivityVDC>
}