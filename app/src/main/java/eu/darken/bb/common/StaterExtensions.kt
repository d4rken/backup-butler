package eu.darken.bb.common

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

fun <T> Observable<T>.withStater(stater: Stater<*>): () -> Disposable? {
    var disp: Disposable? = null
    stater.addLiveDep {
        this.subscribe().also { disp = it }
    }
    return { disp }
}