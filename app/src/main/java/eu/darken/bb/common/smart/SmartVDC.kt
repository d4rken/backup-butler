package eu.darken.bb.common.smart

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import eu.darken.bb.common.coroutine.DefaultDispatcherProvider
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.Logging.Priority.WARN
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.error.ErrorEventSource
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.vdc.VDC
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import timber.log.Timber
import kotlin.coroutines.CoroutineContext


abstract class SmartVDC(
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
) : VDC() {

    val vdcScope = viewModelScope + dispatcherProvider.Default

    var launchErrorHandler: CoroutineExceptionHandler? = null

    private fun getVDCContext(): CoroutineContext {
        val dispatcher = dispatcherProvider.Default
        return getErrorHandler()?.let { dispatcher + it } ?: dispatcher
    }

    private fun getErrorHandler(): CoroutineExceptionHandler? {
        val handler = launchErrorHandler
        if (handler != null) return handler

        if (this is ErrorEventSource) {
            return CoroutineExceptionHandler { _, ex ->
                log(WARN) { "Error during launch: ${ex.asLog()}" }
                errorEvents.postValue(ex)
            }
        }

        return null
    }

    fun <T : Any> DynamicStateFlow<T>.asLiveData2() = flow.asLiveData2()

    fun <T> Flow<T>.asLiveData2() = this.asLiveData(context = getVDCContext())

    fun launch(
        scope: CoroutineScope = viewModelScope,
        context: CoroutineContext = getVDCContext(),
        block: suspend CoroutineScope.() -> Unit
    ) {
        try {
            scope.launch(context = context, block = block)
        } catch (e: CancellationException) {
            Timber.w(e, "launch()ed coroutine was canceled (scope=%s).", scope)
        }
    }

    open fun <T> Flow<T>.launchInViewModel() = this.launchIn(vdcScope)

}