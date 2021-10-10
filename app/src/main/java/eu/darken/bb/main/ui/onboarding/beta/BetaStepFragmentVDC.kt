package eu.darken.bb.main.ui.onboarding.beta

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.BackupButler
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.main.core.OnboardingSettings
import javax.inject.Inject

@HiltViewModel
class BetaStepFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val onboardingSettings: OnboardingSettings,
    private val backupButler: BackupButler,
) : VDC() {

    val navEvents = SingleLiveEvent<NavDirections>()

    fun onContinue() {
        onboardingSettings.lastBetaDisclaimerVersion = backupButler.appInfo.versionCode
        if (onboardingSettings.lastOnboardingVersion > 0L) {
            navEvents.postValue(BetaStepFragmentDirections.actionBetaStepFragmentToInitFragment())
        } else {
            navEvents.postValue(BetaStepFragmentDirections.actionBetaStepFragmentToHelloStepFragment())
        }
    }

}
