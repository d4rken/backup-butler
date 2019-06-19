package eu.darken.bb.main.ui.overview

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import eu.darken.bb.BackupButler
import eu.darken.bb.common.SmartViewModel
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.main.core.service.BackupService
import eu.darken.bb.upgrades.UpgradeControl
import eu.darken.bb.upgrades.UpgradeData
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class OverviewFragmentViewModel @Inject constructor(
        private val butler: BackupButler,
        private val upgradeControl: UpgradeControl,
        @AppContext private val context: Context
) : SmartViewModel() {

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
        context.startService(Intent(context, BackupService::class.java))
    }

    data class AppState(
            val appInfo: BackupButler.AppInfo,
            val upgradeData: UpgradeData
    )
}