package eu.darken.bb.main.ui

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.BackupButler
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.main.core.UISettings
import javax.inject.Inject

@HiltViewModel
class InitFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val butler: BackupButler,
    private val uiSettings: UISettings
) : SmartVDC() {

    val navEvents = SingleLiveEvent<NavDirections>()


    init {
        when {
            uiSettings.showOnboarding -> InitFragmentDirections.actionInitFragmentToHelloStepFragment()
            uiSettings.startMode == UISettings.StartMode.SIMPLE -> InitFragmentDirections.actionInitFragmentToSimpleModeFragment()
            uiSettings.startMode == UISettings.StartMode.ADVANCED -> InitFragmentDirections.actionInitFragmentToAdvancedModeFragment()
            else -> throw IllegalStateException("Unexpected init conditions: $uiSettings")
        }.run { navEvents.postValue(this) }
    }

}