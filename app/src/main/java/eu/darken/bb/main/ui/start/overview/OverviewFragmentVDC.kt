package eu.darken.bb.main.ui.start.overview

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.BackupButler
import eu.darken.bb.common.Stater
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.upgrades.UpgradeControl
import eu.darken.bb.upgrades.UpgradeData
import io.reactivex.rxkotlin.Observables

class OverviewFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val butler: BackupButler,
        private val upgradeControl: UpgradeControl,
        @AppContext private val context: Context
) : SmartVDC() {

    val appState: LiveData<AppState> = Observables
            .zip(butler.appInfo.toObservable(), upgradeControl.upgradeData)
            .map { (appInfo, upgradeData) ->
                return@map AppState(appInfo = appInfo, upgradeData = upgradeData)
            }
            .toLiveData()

    private val updateStater = Stater(UpdateState())
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

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<OverviewFragmentVDC>
}