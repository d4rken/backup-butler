package eu.darken.bb.main.ui.overview

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.BackupButler
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
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
    @ApplicationContext private val context: Context
) : SmartVDC() {

    val appState: LiveData<AppState> = Observables
        .zip(Single.fromCallable { butler.appInfo }.toObservable(), upgradeControl.upgradeData)
        .map { (appInfo, upgradeData) ->
            return@map AppState(appInfo = appInfo, upgradeData = upgradeData)
        }
        .asLiveData()

    private val updateStater = Stater { UpdateState() }
    val updateState = updateStater.liveData

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
}