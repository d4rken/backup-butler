package eu.darken.bb.common

import androidx.lifecycle.LiveData
import eu.darken.bb.common.rx.toLiveData
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class StateUpdater<T>(
        startValue: T
) {
    private val updatePub = PublishSubject.create<(T) -> T?>()
    private val statePub = BehaviorSubject.createDefault<T>(startValue)
    val state: LiveData<T> = statePub.hide().toLiveData()


    init {
        updatePub
                .observeOn(Schedulers.computation())
                .flatMap { action ->
                    statePub.take(1).map { oldState ->
                        val newState = action.invoke(oldState)
                        when {
                            newState != null -> newState
                            else -> oldState
                        }
                    }
                }
                .subscribe { statePub.onNext(it) }
    }

    fun update(action: (T) -> T?) {
        updatePub.onNext(action)
    }

    fun set(state: T) {
        updatePub.onNext { state }
    }

}