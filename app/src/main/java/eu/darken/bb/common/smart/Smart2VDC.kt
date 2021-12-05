package eu.darken.bb.common.smart

import androidx.navigation.NavDirections
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.error.ErrorEventSource
import eu.darken.bb.common.flow.setupCommonEventHandlers
import eu.darken.bb.common.navigation.NavEventSource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn


abstract class Smart2VDC(
    dispatcherProvider: DispatcherProvider,
) : SmartVDC(dispatcherProvider), NavEventSource, ErrorEventSource {

    override val navEvents = SingleLiveEvent<NavDirections?>()

    // TODO Add indirection print and log all posted value
    override val errorEvents = SingleLiveEvent<Throwable>()

    init {
        launchErrorHandler = CoroutineExceptionHandler { _, ex ->
            log(tag) { "Error during launch: ${ex.asLog()}" }
            errorEvents.postValue(ex)
        }
    }

    override fun <T> Flow<T>.launchInViewModel() = this
        .setupCommonEventHandlers(tag) { "launchInViewModel()" }
        .launchIn(vdcScope)

}