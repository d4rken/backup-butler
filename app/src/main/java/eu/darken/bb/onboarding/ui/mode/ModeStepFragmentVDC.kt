package eu.darken.bb.onboarding.ui.mode

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.BackupButler
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.onboarding.core.OnboardingSettings
import javax.inject.Inject

@HiltViewModel
class ModeStepFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val uiSettings: UISettings,
    private val onboardingSettings: OnboardingSettings,
    private val backupButler: BackupButler,
) : VDC() {

    val finishOnboardingEvent = SingleLiveEvent<Unit>()

    fun onSimpleSelected() {
        finishOnboarding(UISettings.StartMode.QUICK)
    }

    fun onAdvancedSelected() {
        finishOnboarding(UISettings.StartMode.NORMAL)
    }

    private fun finishOnboarding(startMode: UISettings.StartMode) {
        onboardingSettings.lastOnboardingVersion = backupButler.appInfo.versionCode
        uiSettings.startMode = startMode
        finishOnboardingEvent.postValue(Unit)
    }
}