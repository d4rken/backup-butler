package eu.darken.bb.main.ui.advanced

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.vdc.VDC
import javax.inject.Inject

@HiltViewModel
class AdvancedModeFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle
) : VDC() {

    val state: MutableLiveData<State> = MutableLiveData(State(false))

    fun onGo() {
        state.postValue(state.value!!.copy(ready = true))
    }

    data class State(val ready: Boolean)
}