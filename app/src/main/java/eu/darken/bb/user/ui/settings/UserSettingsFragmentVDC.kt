package eu.darken.bb.user.ui.settings

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.smart.SmartVDC
import javax.inject.Inject

@HiltViewModel
class UserSettingsFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val bbDebug: BBDebug,
    private val dispatcherProvider: DispatcherProvider,
) : SmartVDC(dispatcherProvider) {

    companion object {
        private val TAG = logTag("User", "Settings", "VDC")
    }
}