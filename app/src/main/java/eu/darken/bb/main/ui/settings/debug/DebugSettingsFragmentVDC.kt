package eu.darken.bb.main.ui.settings.debug

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.debug.BBDebug

class DebugSettingsFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val bbDebug: BBDebug
) : SmartVDC() {
    private val stater = Stater(State())
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

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<DebugSettingsFragmentVDC>
}