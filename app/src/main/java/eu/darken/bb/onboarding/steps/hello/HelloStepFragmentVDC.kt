package eu.darken.bb.onboarding.steps.hello

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.VDC

class HelloStepFragmentVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle
) : VDC() {

    val state = MutableLiveData<State>(State(emoji = ""))

    data class State(val emoji: String)

    @AssistedFactory
    interface Factory : SavedStateVDCFactory<HelloStepFragmentVDC>
}