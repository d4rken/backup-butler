package eu.darken.bb.common.rx

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
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
        val distinct = if (last != null) check.invoke(last, newEmission) else true
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

object Observables2 {
    fun <T> fromCallableSafe(producer: () -> T): Observable<T> {
        return Observable.create<T> {
            try {
                it.onNext(producer.invoke())
                it.onComplete()
            } catch (e: Throwable) {
                it.tryOnError(e)
            }
        }
    }
}

