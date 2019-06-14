package eu.darken.bb.main.ui.fragment

import androidx.lifecycle.LiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import eu.darken.bb.BackupButler
import eu.darken.bb.common.SmartViewModel
import eu.darken.bb.upgrades.UpgradeControl
import eu.darken.bb.upgrades.UpgradeData
import eu.darken.bb.workers.DefaultBackupWorker
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class OverviewFragmentViewModel @Inject constructor(
        private val butler: BackupButler,
        private val upgradeControl: UpgradeControl,
        private val workManager: WorkManager
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
        workManager.enqueue(OneTimeWorkRequestBuilder<DefaultBackupWorker>().build())
    }

    data class AppState(
            val appInfo: BackupButler.AppInfo,
            val upgradeData: UpgradeData
    )
}