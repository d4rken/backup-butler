package eu.darken.bb.onboarding.steps

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class HelloStepFragmentViewModel @Inject constructor(

) : ViewModel() {

    val state = MutableLiveData<State>(State(emoji = ""))

    data class State(val emoji: String)
}