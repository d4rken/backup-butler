package eu.darken.bb.user.ui.settings

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.Stater
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import javax.inject.Inject

@HiltViewModel
class UserSettingsFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val bbDebug: BBDebug
) : SmartVDC() {
    private val stater = Stater { State() }
    val state = stater.liveData

    init {
        bbDebug.observeOptions()
            .subscribe { options ->
                stater.update {
                    it.copy(
                        isRecording = options.isRecording,
                        recordingPath = options.recorderPath ?: ""
                    )
                }
            }
            .withScopeVDC(this)
    }

    fun startDebugLog() {
        bbDebug.setRecording(true)
    }

    data class State(
        val isRecording: Boolean = false,
        val recordingPath: String = ""
    )
}