package eu.darken.bb.common

import eu.darken.bb.App
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.Executors

open class HotData<T>(
        initialValue: () -> T
) {
    constructor(initialValue: T) : this({ initialValue })

    private val scheduler = Schedulers.from(Executors.newSingleThreadExecutor())

    private val updatePub = PublishSubject.create<(T) -> T>()
    private val statePub = BehaviorSubject.create<T>()

    init {
        Single
                .fromCallable { initialValue.invoke() }
                .subscribeOn(scheduler)
                .subscribe { value -> statePub.onNext(value) }
    }

    val snapshot: T
        get() = data.blockingFirst()

    val data: Observable<T> = statePub.hide()

    init {
        updatePub
                .observeOn(scheduler)
                .flatMap { action ->
                    statePub.take(1).map { oldState ->
                        val newState = action.invoke(oldState)
                        Timber.tag(TAG).v("Update $oldState -> $newState")
                        when {
                            newState != null -> newState
                            else -> oldState
                        }
                    }
                }
                .subscribe { statePub.onNext(it) }
    }

    fun update(action: (T) -> T) {
        updatePub.onNext(action)
    }

    fun updateRx(action: (T) -> T): Single<T> = Single.create<T> { emitter ->
        val wrap: (T) -> T = { oldValue ->
            try {
                val newValue = action.invoke(oldValue)
                emitter.onSuccess(newValue)
                newValue
            } catch (e: Throwable) {
                emitter.onError(e)
                oldValue
            }
        }
        update(wrap)
    }

    companion object {
        private val TAG = App.logTag("HotData")
    }
}