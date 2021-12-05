package eu.darken.bb.common.flow

import eu.darken.bb.common.coroutine.cancelAfterRun
import eu.darken.bb.common.debug.logging.Logging.Priority.ERROR
import eu.darken.bb.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.error.hasCause
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlin.time.Duration

/**
 * Create a stateful flow, with the initial value of null, but never emits a null value.
 * Helper method to create a new flow without suspending and without initial value
 * The flow collector will just wait for the first value
 */
fun <T : Any> Flow<T>.shareLatest(
    tag: String? = null,
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.WhileSubscribed(replayExpirationMillis = 0)
) = this
    .onStart { if (tag != null) log(tag) { "shareLatest(...) start" } }
    .onEach { if (tag != null) log(tag) { "shareLatest(...) emission: $it" } }
    .onCompletion { if (tag != null) log(tag) { "shareLatest(...) completed." } }
    .catch {
        if (tag != null) log(tag) { "shareLatest(...) catch(): ${it.asLog()}" }
        throw it
    }
    .stateIn(
        scope = scope,
        started = started,
        initialValue = null
    )
    .filterNotNull()

fun <T : Any> Flow<T>.replayingShare(scope: CoroutineScope) = this.shareIn(
    scope = scope,
    replay = 1,
    started = SharingStarted.WhileSubscribed(replayExpiration = Duration.ZERO)
)

suspend fun <T> Flow<*>.launchForAction(scope: CoroutineScope, action: suspend () -> T): T = this
    .launchIn(scope).cancelAfterRun(action)

fun <T> Flow<T>.onErrorMixLast(mixer: (last: T?, error: Throwable) -> T): Flow<T> =
    materialize().withPrevious().flatMapConcat { (previous, current) ->
        when (current) {
            is Notification.Complete -> emptyFlow()
            is Notification.Error<*, *> -> {
                // Maybe be null if no initial items emitted
                val previousValue = (previous as? Notification.Result)?.value
                val cause = current.cause
                flowOf(mixer(previousValue, cause))
            }
            is Notification.Result -> flowOf(current.value)
        }
    }

internal fun <T> Flow<T>.withPrevious(): Flow<Pair<T?, T>> = this
    .scan(Pair<T?, T?>(null, null)) { previous, current -> Pair(previous.second, current) }
    .drop(1)
    .map {
        @Suppress("UNCHECKED_CAST")
        it as Pair<T?, T>
    }


fun <T> Flow<T>.onError(block: suspend (Throwable) -> Unit) = this.catch {
    block(it)
    throw it
}

fun <T> Flow<T>.takeUntilAfter(predicate: suspend (T) -> Boolean) = transformWhile {
    val fullfilled = predicate(it)
    emit(it)
    !fullfilled // We keep emitting until condition is fullfilled = true
}

fun <T> Flow<T>.setupCommonEventHandlers(tag: String, identifier: () -> String) = this
    .onStart { log(tag, VERBOSE) { "${identifier()}.onStart()" } }
    .onCompletion { log(tag, VERBOSE) { "${identifier()}.onCompletion()" } }
    .catch {
        if (it.hasCause(CancellationException::class)) {
            log(tag, VERBOSE) { "${identifier()} cancelled" }
        } else {
            log(tag, ERROR) { "${identifier()} failed: ${it.asLog()}" }
            throw it
        }
    }

