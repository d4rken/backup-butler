package eu.darken.bb.main.ui.onboarding.mode

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.BackupButler
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.main.core.OnboardingSettings
import eu.darken.bb.main.core.UISettings
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