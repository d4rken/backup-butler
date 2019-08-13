package eu.darken.bb.main.ui.start

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.SavedStateVDCFactory


class MainActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle
) : VDC() {

    val state: MutableLiveData<State> = MutableLiveData(State(false))

    fun onGo() {
        state.postValue(state.value!!.copy(ready = true))
    }

    data class State(val ready: Boolean)

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<MainActivityVDC>
}