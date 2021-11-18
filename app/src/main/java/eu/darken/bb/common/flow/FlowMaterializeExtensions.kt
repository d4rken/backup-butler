package eu.darken.bb.common.flow

import kotlinx.coroutines.flow.*


sealed interface Notification<T> {
    data class Result<T>(val value: T) : Notification<T>
    data class Error<T, E : Throwable>(val cause: E) : Notification<T>
    class Complete<T> : Notification<T>
}

fun <T> Flow<T>.materialize(): Flow<Notification<T>> =
    map<T, Notification<T>> { Notification.Result(it) }
        .onCompletion { cause ->
            if (cause == null) emit(Notification.Complete())
            else emit(Notification.Error(cause))
        }
        .catch { e -> emit(Notification.Error(e)) }

fun <T> Flow<Notification<T>>.dematerialize(): Flow<T> =
    takeWhile { it !is Notification.Complete }
        .map {
            when (it) {
                is Notification.Result -> it.value
                is Notification.Error<*, *> -> throw it.cause
                is Notification.Complete -> throw RuntimeException("Unreachable!")
            }
        }