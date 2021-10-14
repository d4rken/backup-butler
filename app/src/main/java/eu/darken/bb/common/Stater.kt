package eu.darken.bb.common

import androidx.lifecycle.LiveData
import eu.darken.bb.common.rx.asLiveData
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

class Stater<T : Any>(
    tag: String? = null,
    startValueProvider: () -> T
) : HotData<T>(
    tag = tag,
    initial = startValueProvider
) {

    private val liveDeps = mutableSetOf<() -> Disposable>()
    private var liveDepCompDis = CompositeDisposable()

    fun addLiveDataScopedDep(dep: () -> Disposable): Stater<T> = apply {
        synchronized(liveDeps) {
            liveDeps.add(dep)
        }
    }

    val liveData: LiveData<T> = data
        .doOnSubscribe {
            synchronized(liveDeps) {
                liveDeps.forEach { liveDepCompDis.add(it.invoke()) }
            }
        }
        .doFinally {
            synchronized(liveDeps) {
                liveDepCompDis.dispose()
                liveDepCompDis = CompositeDisposable()
            }
        }
        .asLiveData()

}

