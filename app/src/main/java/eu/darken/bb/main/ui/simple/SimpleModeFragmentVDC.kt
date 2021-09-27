package eu.darken.bb.main.ui.simple

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.ReportABug
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.main.core.UISettings
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class SimpleModeFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val reportABug: ReportABug,
    private val uiSettings: UISettings,
    private val bbDebug: BBDebug,
) : SmartVDC() {

    val navEvents = SingleLiveEvent<NavDirections>()
    val state = Observable
        .combineLatest(
            bbDebug.observeOptions(),
            uiSettings.showDebugPage.observable
        ) { debug, showDebug ->
            State(
                showDebugStuff = showDebug || BBDebug.isDebug(),
                isRecordingDebug = debug.isRecording
            )
        }
        .subscribeOn(Schedulers.computation())
        .toLiveData()

    data class State(
        val showDebugStuff: Boolean,
        val isRecordingDebug: Boolean
    )

    fun switchToAdvancedMode() {
        uiSettings.startMode = UISettings.StartMode.ADVANCED
        navEvents.postValue(SimpleModeFragmentDirections.actionSimpleModeFragmentToAdvancedModeFragment())
    }

    fun reportBug() {
        reportABug.reportABug()
    }

    fun recordDebugLog() {
        bbDebug.setRecording(true)
    }
}