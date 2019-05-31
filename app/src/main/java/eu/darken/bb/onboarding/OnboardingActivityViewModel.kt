package eu.darken.bb.onboarding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject


class OnboardingActivityViewModel @Inject constructor(

) : ViewModel() {

    val state: MutableLiveData<State> = MutableLiveData(State(State.Step.HELLO))

    data class State(val step: Step) {

        enum class Step {
            HELLO
        }
    }
}