package eu.darken.bb.processor.ui.progress

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.smart.SmartVDC
import eu.darken.bb.processor.core.ProcessorControl
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ProgressFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    backupServiceControl: ProcessorControl,
    private val dispatcherProvider: DispatcherProvider
) : SmartVDC(dispatcherProvider) {

    private val progressHost = backupServiceControl.progressHost
        .filterNotNull()
        .catch { }
// TODO       .timeout(3, TimeUnit.SECONDS)


    private val stater = DynamicStateFlow(TAG, vdcScope) { State() }
    val state = stater.asLiveData2()

    val finishEvent = SingleLiveEvent<Any>()

    init {
        progressHost
            .take(1)
            .flatMapConcat { it.progress }
            .onCompletion { finishEvent.postValue(Any()) }
            .onEach { progress ->
                stater.updateBlocking {
                    copy(
                        taskProgress = progress,
                        actionProgress = progress.child
                    )
                }
            }
            .launchInViewModel()
    }

    data class State(
        val taskProgress: Progress.Data = Progress.Data(),
        val actionProgress: Progress.Data? = null
    )

    companion object {
        private val TAG = logTag("Progress", "Fragment", "VDC")
    }
}