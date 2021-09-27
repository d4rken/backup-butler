package eu.darken.bb.main.ui.onboarding.mode

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.main.core.UISettings
import javax.inject.Inject

@HiltViewModel
class ModeStepFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val uiSettings: UISettings,
) : VDC() {

    val finishOnboardingEvent = SingleLiveEvent<Unit>()

    fun onSimpleSelected() {
        uiSettings.showOnboarding = false
        uiSettings.startMode = UISettings.StartMode.SIMPLE
        finishOnboardingEvent.postValue(Unit)
    }

    fun onAdvancedSelected() {
        uiSettings.showOnboarding = false
        uiSettings.startMode = UISettings.StartMode.ADVANCED
        finishOnboardingEvent.postValue(Unit)
    }
}