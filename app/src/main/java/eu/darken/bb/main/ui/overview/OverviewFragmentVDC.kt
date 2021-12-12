package eu.darken.bb.main.ui.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.BackupButler
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.user.core.UpgradeControl
import eu.darken.bb.user.core.UpgradeData
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.Observables
import javax.inject.Inject

@HiltViewModel
class OverviewFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val butler: BackupButler,
    private val upgradeControl: UpgradeControl,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    val appState: LiveData<AppState> = Observables
        .zip(Single.fromCallable { butler.appInfo }.toObservable(), upgradeControl.upgradeData)
        .map { (appInfo, upgradeData) ->
            return@map AppState(appInfo = appInfo, upgradeData = upgradeData)
        }
        .asLiveData()

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
        val upgradeData: UpgradeData
    )

    data class UpdateState(
        val available: Boolean = false
    )

    companion object {
        private val TAG = logTag("Overview", "VDC")
    }
}