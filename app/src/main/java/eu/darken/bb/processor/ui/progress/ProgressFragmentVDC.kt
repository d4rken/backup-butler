package eu.darken.bb.processor.ui.progress

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.processor.core.ProcessorControl
import io.reactivex.schedulers.Schedulers

class ProgressFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        backupServiceControl: ProcessorControl
) : SmartVDC() {
    private val progressHostObs = backupServiceControl.progressHost
            .subscribeOn(Schedulers.io())
            .filter { it.isNotNull }
            .map { it.value }


    private val progressObs = progressHostObs
            .firstOrError()
            .flatMapObservable { it.progress }
            .doOnNext { progress ->
                stater.update { state ->
                    state.copy(
                            taskProgress = progress,
                            actionProgress = progress.child
                    )
                }
            }
            .doOnTerminate {
                finishEvent.postValue(Any())
            }


    private val stater: Stater<State> = Stater(State())
            .addLiveDep { progressObs.subscribe() }

    val state = stater.liveData

    val finishEvent = SingleLiveEvent<Any>()

    data class State(
            val taskProgress: Progress.Data = Progress.Data(),
            val actionProgress: Progress.Data? = null
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<ProgressFragmentVDC>
}