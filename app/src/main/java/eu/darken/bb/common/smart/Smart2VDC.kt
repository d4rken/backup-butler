package eu.darken.bb.common.smart

import androidx.navigation.NavDirections
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.error.ErrorEventSource
import eu.darken.bb.common.navigation.NavEventSource


abstract class Smart2VDC(
    dispatcherProvider: DispatcherProvider,
) : SmartVDC(dispatcherProvider), NavEventSource, ErrorEventSource {

    override val navEvents = SingleLiveEvent<NavDirections?>()

    // TODO Add indirection print and log all posted value
    override val errorEvents = SingleLiveEvent<Throwable>()

}