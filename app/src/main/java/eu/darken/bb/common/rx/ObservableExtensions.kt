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

fun <T> Observable<T>.onErrorComplete(action: ((Throwable) -> Unit)? = null): Observable<T> {
    val call: (Throwable) -> ObservableSource<out T> = {
        Timber.d(it, "Swalled error (onErrorComplete)")
        action?.invoke(it)
        Observable.empty()
    }
    return onErrorResumeNext(call)
}