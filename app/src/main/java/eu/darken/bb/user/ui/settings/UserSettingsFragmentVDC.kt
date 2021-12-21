package eu.darken.bb.user.ui.settings

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.user.core.UpgradeControl
import javax.inject.Inject

@HiltViewModel
class UserSettingsFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val bbDebug: BBDebug,
    private val upgradeControl: UpgradeControl,
    dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    val state = upgradeControl.state.asLiveData2()

    companion object {
        private val TAG = logTag("User", "Settings", "VDC")
    }
}