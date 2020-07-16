package eu.darken.bb.common.vdc

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable


abstract class SmartVDC : VDC() {
    private val vdcSubs = CompositeDisposable()

    fun addVdcDisp(disposable: Disposable) {
        vdcSubs.add(disposable)
    }

    override fun onCleared() {
        vdcSubs.dispose()
        super.onCleared()
    }
}