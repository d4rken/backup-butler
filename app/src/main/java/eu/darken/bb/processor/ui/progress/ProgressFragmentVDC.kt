package eu.darken.bb.processor.ui.progress

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.processor.core.ProcessorControl
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class ProgressFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        backupServiceControl: ProcessorControl
) : SmartVDC() {
    private val progressHostObs = backupServiceControl.progressHost
            .subscribeOn(Schedulers.io())
            .filter { it.isNotNull }
            .map { it.value!! }
            .timeout(3, TimeUnit.SECONDS)
            .onErrorComplete()

    private val stater: Stater<State> = Stater(State())
    val state = stater.liveData

    val finishEvent = SingleLiveEvent<Any>()

    init {
        progressHostObs
                .take(1)
                .flatMap { it.progress }
                .doOnTerminate { finishEvent.postValue(Any()) }
                .subscribe { progress ->
                    stater.update { state ->
                        state.copy(
                                taskProgress = progress,
                                actionProgress = progress.child
                        )
                    }
                }
                .withScopeVDC(this)
    }

    data class State(
            val taskProgress: Progress.Data = Progress.Data(),
            val actionProgress: Progress.Data? = null
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<ProgressFragmentVDC>
}