package eu.darken.bb.settings.ui.general

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.smart.SmartVDC
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val bbDebug: BBDebug,
    private val dispatcherProvider: DispatcherProvider,
) : SmartVDC(dispatcherProvider) {

    private val stater = DynamicStateFlow(TAG, vdcScope) { State() }
    val state = stater.asLiveData2()

    init {
        bbDebug.observeOptions()
            .onEach { options ->
                stater.updateBlocking {
                    copy(
                        isRecording = options.isRecording,
                        recordingPath = options.recorderPath ?: ""
                    )
                }
            }
            .launchInViewModel()
    }

    fun startDebugLog() {
        bbDebug.setRecording(true)
    }

    data class State(
        val isRecording: Boolean = false,
        val recordingPath: String = ""
    )

    companion object {
        private val TAG = logTag("Settings", "General", "VDC")
    }
}