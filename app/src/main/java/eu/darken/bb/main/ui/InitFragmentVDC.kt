package eu.darken.bb.main.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.BackupButler
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.main.core.UISettings
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

@HiltViewModel
class InitFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val butler: BackupButler,
    private val uiSettings: UISettings
) : SmartVDC() {

    val launchConfig: LiveData<LaunchConfig> = Observable.just(
        LaunchConfig(
            startMode = uiSettings.startMode,
            showOnboarding = uiSettings.showOnboarding
        )
    ).toLiveData()

    data class LaunchConfig(
        val startMode: UISettings.StartMode,
        val showOnboarding: Boolean,
    )
}