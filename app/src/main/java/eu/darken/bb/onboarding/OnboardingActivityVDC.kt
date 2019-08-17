package eu.darken.bb.onboarding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.VDC


class OnboardingActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle
) : VDC() {

    val state: MutableLiveData<State> = MutableLiveData(State(State.Step.HELLO))

    data class State(val step: Step) {

        enum class Step {
            HELLO
        }
    }


    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<OnboardingActivityVDC>
}