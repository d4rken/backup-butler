package eu.darken.bb.onboarding.steps.hello

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.vdc.VDC
import javax.inject.Inject

@HiltViewModel
class HelloStepFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle
) : VDC() {

    val state = MutableLiveData<State>(State(emoji = ""))

    data class State(val emoji: String)
}