package eu.darken.bb.main.ui.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.BackupButler
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.user.core.UpgradeControl
import eu.darken.bb.user.core.UpgradeInfo
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class OverviewFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val butler: BackupButler,
    upgradeControl: UpgradeControl,
    dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    val appState: LiveData<AppState> = combine(
        flow { emit(butler.appInfo) },
        upgradeControl.state
    ) { appInfo, upgradeData ->
        AppState(appInfo = appInfo, upgradeInfo = upgradeData)
    }.asLiveData2()

    private val updateStater = DynamicStateFlow(TAG, vdcScope) { UpdateState() }
    val updateState = updateStater.asLiveData2()

    fun onChangelog() {
        TODO("not implemented")
    }

    fun onUpdate() {
        TODO("not implemented")
    }

    data class AppState(
        val appInfo: BackupButler.AppInfo,
        val upgradeInfo: UpgradeInfo
    )

    data class UpdateState(
        val available: Boolean = false
    )

    companion object {
        private val TAG = logTag("Overview", "VDC")
    }
}