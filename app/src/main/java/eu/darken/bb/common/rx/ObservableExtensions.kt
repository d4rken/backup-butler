package eu.darken.bb.common.rx

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import eu.darken.bb.common.getRootCause
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.ObservableSource
import timber.log.Timber

fun <T> Observable<T>.toLiveData(): LiveData<T> {
    return LiveDataReactiveStreams.fromPublisher(this.toFlowable(BackpressureStrategy.ERROR))
}

fun <T> Observable<T>.filterUnchanged(check: (T, T) -> Boolean = { it1, it2 -> it1 != it2 }): Observable<T> {
    var lastEmission: T? = null
    return filter { newEmission ->
        val last = lastEmission
        val distinct = if (last != null) check(last, newEmission) else true
        lastEmission = newEmission
        return@filter distinct
    }
}

fun <T> Observable<T>.onErrorComplete(
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

fun <T> Observable<T>.swallowInterrupts() = onErrorComplete { it is InterruptedException || it.getRootCause() is InterruptedException }

fun <T> Observable<T>.withPrevious(): Observable<Pair<T?, T>> =
        this.scan(Pair<T?, T?>(null, null)) { previous, current -> Pair(previous.second, current) }
                .skip(1)
                .map {
                    @Suppress("UNCHECKED_CAST")
                    it as Pair<T?, T>
                }

fun <T> Observable<T>.onErrorMixLast(mixer: (last: T?, error: Throwable) -> T): Observable<T> =
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