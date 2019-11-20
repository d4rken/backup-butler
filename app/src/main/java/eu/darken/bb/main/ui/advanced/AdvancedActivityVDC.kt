package eu.darken.bb.main.ui.advanced

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
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

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<AdvancedActivityVDC>
}