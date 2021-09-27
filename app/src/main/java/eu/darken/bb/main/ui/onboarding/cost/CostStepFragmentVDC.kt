package eu.darken.bb.main.ui.onboarding.cost

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.vdc.VDC
import javax.inject.Inject

@HiltViewModel
class CostStepFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle
) : VDC() {

    val navEvents = SingleLiveEvent<NavDirections>()

    fun onContinue() {
        navEvents.postValue(CostStepFragmentDirections.actionCostStepFragmentToModeStepFragment())
    }

}
