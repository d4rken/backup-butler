package eu.darken.bb.onboarding.steps

import androidx.lifecycle.MutableLiveData
import eu.darken.bb.common.VDC
import javax.inject.Inject

class HelloStepFragmentVDC @Inject constructor(

) : VDC() {

    val state = MutableLiveData<State>(State(emoji = ""))

    data class State(val emoji: String)
}