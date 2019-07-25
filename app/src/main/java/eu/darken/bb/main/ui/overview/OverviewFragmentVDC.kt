package eu.darken.bb.main.ui.overview

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.BackupButler
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.upgrades.UpgradeControl
import eu.darken.bb.upgrades.UpgradeData
import io.reactivex.rxkotlin.Observables

class OverviewFragmentVDC @AssistedInject constructor(
        private val butler: BackupButler,
        private val upgradeControl: UpgradeControl,
        @AppContext private val context: Context,
        @Assisted private val handle: SavedStateHandle
) : SmartVDC() {

    val appState: LiveData<AppState> = Observables
            .zip(butler.appInfo.toObservable(), upgradeControl.upgradeData)
            .map { (appInfo, upgradeData) ->
                return@map AppState(
                        appInfo = appInfo,
                        upgradeData = upgradeData
                )
            }
            .toLiveData()

    init {

    }

    fun test() {

    }

    data class AppState(
            val appInfo: BackupButler.AppInfo,
            val upgradeData: UpgradeData
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<OverviewFragmentVDC>
}