package eu.darken.bb.common.rx

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import eu.darken.bb.common.errors.getRootCause
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Single
import timber.log.Timber

fun <T : Any> Observable<T>.asLiveData(): LiveData<T> {
    return LiveDataReactiveStreams.fromPublisher(this.toFlowable(BackpressureStrategy.ERROR))
}

fun <T : Any> Observable<T>.filterUnchanged(check: (T, T) -> Boolean = { it1, it2 -> it1 != it2 }): Observable<T> {
    var lastEmission: T? = null
    return filter { newEmission ->
        val last = lastEmission
        val distinct = if (last != null) check(last, newEmission) else true
        lastEmission = newEmission
        return@filter distinct
    }
}

fun <T : Any> Observable<T>.onErrorComplete(
    action: ((Throwable) -> Unit)? = null,
    condition: ((Throwable) -> Boolean)? = null
): Observable<T> {
    val call: (Throwable) -> ObservableSource<out T> = {
        if (condition?.invoke(it) == false) {
            throw it
        }
        Timber.d(it, "Swallowed error (onErrorComplete)")
        action?.invoke(it)
        Observable.empty()
    }
    return onErrorResumeNext(call)
}

fun <T : Any> Observable<T>.swallowInterruptExceptions() =
    onErrorComplete { it is InterruptedException || it.getRootCause() is InterruptedException }

fun <T : Any> Observable<T>.onErrorMixLast(mixer: (last: T?, error: Throwable) -> T): Observable<T> =
    materialize().withPrevious().flatMap { (previous, current) ->
        when {
            current.isOnComplete -> Observable.empty<T>()
            current.isOnError -> {
                val value = current.value ?: previous?.value
                val error = current.error ?: previous?.error
                Observable.just(mixer(value, error!!))
            }
            else -> Observable.just(current.value)
        }
    }

internal fun <T : Any> Observable<T>.latest(): Single<T> = take(1).singleOrError()

internal fun <T : Any> Observable<T>.withPrevious(): Observable<Pair<T?, T>> =
    this.scan(Pair<T?, T?>(null, null)) { previous, current -> Pair(previous.second, current) }
        .skip(1)
        .map {
            @Suppress("UNCHECKED_CAST")
            it as Pair<T?, T>
        }

internal fun <T : Any> Observable<T>.filterEqual(check: (old: T, new: T) -> Boolean = { it1, it2 -> it1 != it2 }): Observable<T> =
    withPrevious()
        .filter { (old, new) ->
            return@filter if (old != null) check(old, new) else true
        }
        .map { it.second }