package eu.darken.bb.main.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.dagger.SavedStateVDCFactory


class MainActivityViewModel @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle
) : ViewModel() {

    val state: MutableLiveData<State> = MutableLiveData(State(false))

    fun onGo() {
        state.postValue(state.value!!.copy(ready = true))
    }

    data class State(val ready: Boolean)

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<MainActivityViewModel>
}