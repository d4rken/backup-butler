package eu.darken.bb.main.ui

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.BackupButler
import eu.darken.bb.common.BuildConfigWrap
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.onboarding.core.OnboardingSettings
import javax.inject.Inject

@HiltViewModel
class InitFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val butler: BackupButler,
    private val onboardingSettings: OnboardingSettings,
    private val uiSettings: UISettings,
) : SmartVDC() {

    val navEvents = SingleLiveEvent<NavDirections>()
    val finishSplashScreen = SingleLiveEvent<Any>()

    init {
        // TODO If beta version and not shown yet for this version, show beta disclaimer

        val shouldShowBeta =
            BuildConfigWrap.isBetaBuild && onboardingSettings.lastBetaDisclaimerVersion != butler.appInfo.versionCode
        val shouldShowChangelog = false // TODO

        when {
            shouldShowBeta -> InitFragmentDirections.actionInitFragmentToBetaStepFragment()
            onboardingSettings.lastOnboardingVersion == 0L -> InitFragmentDirections.actionInitFragmentToHelloStepFragment()
            uiSettings.startMode == UISettings.StartMode.QUICK -> InitFragmentDirections.actionInitFragmentToQuickModeFragment()
            uiSettings.startMode == UISettings.StartMode.NORMAL -> InitFragmentDirections.actionInitFragmentToNormalModeFragment()
            else -> throw IllegalStateException("Unexpected init conditions: $uiSettings")
        }.run {
            finishSplashScreen.postValue(Any())
            navEvents.postValue(this)
        }
    }

}