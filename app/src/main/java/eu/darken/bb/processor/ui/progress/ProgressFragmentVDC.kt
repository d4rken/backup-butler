package eu.darken.bb.processor.ui.progress

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.processor.core.ProcessorControl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ProgressFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    backupServiceControl: ProcessorControl,
    private val dispatcherProvider: DispatcherProvider
) : Smart2VDC(dispatcherProvider) {

    private val progressHost = backupServiceControl.progressHost
        .onEach { delay(3000) }
//        .filterNotNull()
//        .catch { }
//// TODO       .timeout(3, TimeUnit.SECONDS)


    private val stater = DynamicStateFlow(TAG, vdcScope) { State() }
    val state = stater.asLiveData2()

    init {
        progressHost
            .flatMapLatest {
                it?.progress ?: throw IllegalStateException("No progress host available.")
            }
            .catch {
                if (it is IllegalStateException) navEvents.postValue(null)
                else throw it
            }
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