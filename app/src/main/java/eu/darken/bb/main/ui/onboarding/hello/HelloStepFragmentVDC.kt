package eu.darken.bb.main.ui.onboarding.hello

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.vdc.VDC
import javax.inject.Inject

@HiltViewModel
class HelloStepFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle
) : VDC() {

    val navEvent = SingleLiveEvent<NavDirections>()

    fun onContinue() {
        navEvent.postValue(HelloStepFragmentDirections.actionHelloStepFragmentToCostStepFragment())
    }

}