package eu.darken.bb.common

import eu.darken.bb.App
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

abstract class SmartVDC : VDC() {

    companion object {
        val TAG: String = App.logTag("SmartVDC")

        inline fun <T> Iterable<T>.firstIgnoreError(errorMsg: String? = null, predicate: (T) -> Boolean): T = try {
            this.first(predicate)
        } catch (e: NoSuchElementException) {
            throw IgnoredException(errorMsg, e)
        }

        class IgnoredException(message: String? = null, throwable: Throwable? = null) : Exception(message, throwable)
    }

    private val scopedSubsMap = mapOf(
            Pair(ScopedSub.Scope.LIFE, CompositeDisposable())
    )

    open class ScopedSub(val scope: Scope) {
        enum class Scope {
            LIFE
        }

        override fun toString(): String = "ScopedSub(data=$scope)"
    }

    open class LifeSub<DataT>(sub: ScopedSub, val data: DataT) : ScopedSub(sub.scope) {

        open operator fun component1(): DataT = data

        override fun toString(): String = "LifeSub(scope=$scope, view=$data)"
    }

    fun <DataT> Observable<DataT>.subLifeScope(next: (LifeSub<DataT>) -> Unit): DisposableObserver<LifeSub<DataT>> {
        return this.subLifeScope(next, null)
    }

    fun <DataT> Observable<DataT>.subLifeScope(next: (LifeSub<DataT>) -> Unit, error: ((Throwable) -> Unit)? = null): DisposableObserver<LifeSub<DataT>> {
        val parent = this
        val scope = ScopedSub.Scope.LIFE
        val observer = object : DisposableObserver<LifeSub<DataT>>() {
            override fun onComplete() {}

            override fun onStart() {
                scopedSubsMap.getValue(scope).add(this)
                super.onStart()
            }

            override fun onNext(t: LifeSub<DataT>) {
                next.invoke(t)
            }

            override fun onError(e: Throwable) {
                error?.invoke(e)
            }
        }

        return Observable.wrap(parent)
                .subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                .map { LifeSub(ScopedSub(scope), it) }
                .subscribeWith(observer)
    }

    override fun onCleared() {
        Timber.v("onCleared()")
        super.onCleared()
    }
}