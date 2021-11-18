package eu.darken.bb.common.error

import eu.darken.bb.common.SingleLiveEvent

interface ErrorEventSource {
    val errorEvents: SingleLiveEvent<Throwable>
}