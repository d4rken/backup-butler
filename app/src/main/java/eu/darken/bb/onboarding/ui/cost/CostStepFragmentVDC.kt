package eu.darken.bb.onboarding.ui.cost

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.BackupButler
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.onboarding.core.OnboardingSettings
import javax.inject.Inject

@HiltViewModel
class CostStepFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
    private val onboardingSettings: OnboardingSettings,
    private val uiSettings: UISettings,
    private val backupButler: BackupButler,
) : Smart2VDC(dispatcherProvider) {

    fun onContinue() {
        onboardingSettings.lastOnboardingVersion = backupButler.appInfo.versionCode
        uiSettings.startMode = UISettings.StartMode.QUICK
        navEvents.postValue(CostStepFragmentDirections.actionCostStepFragmentToInitFragment())
    }

}
