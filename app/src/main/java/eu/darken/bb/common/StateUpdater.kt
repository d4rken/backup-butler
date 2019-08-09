package eu.darken.bb.common

import androidx.lifecycle.LiveData
import eu.darken.bb.common.rx.toLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class StateUpdater<T> : HotData<T> {
    constructor(startValue: T) : super(startValue)
    constructor(startValueProvider: () -> T) : super(startValueProvider)

    private val liveDeps = mutableSetOf<() -> Disposable>()
    private var liveDepCompDis = CompositeDisposable()

    fun addLiveDep(dep: () -> Disposable): StateUpdater<T> = apply {
        liveDeps.add(dep)
    }

    val liveData: LiveData<T> = data
            .doOnSubscribe {
                liveDeps.forEach { liveDepCompDis.add(it.invoke()) }
            }
            .doFinally {
                liveDepCompDis.dispose()
                liveDepCompDis = CompositeDisposable()
            }
            .toLiveData()
}