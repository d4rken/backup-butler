package eu.darken.bb.common.rx

import eu.darken.bb.common.Stater
import eu.darken.bb.common.vdc.SmartVDC
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

fun Disposable.withCompositeDisposable(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}

fun Disposable.withScopeVDC(vdc: SmartVDC) {
    vdc.addVdcDisp(this)
}

fun <T> Observable<T>.withScopeLiveData(stater: Stater<*>): () -> Disposable? {
    var disp: Disposable? = null
    stater.addLiveDataScopedDep {
        this.subscribe().also { disp = it }
    }
    return { disp }
}

fun <T> Disposable.withScopeThis(action: () -> T): T {
    return try {
        action()
    } finally {
        dispose()
    }
}