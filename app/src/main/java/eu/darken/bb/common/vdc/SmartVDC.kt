package eu.darken.bb.common.vdc

import androidx.lifecycle.viewModelScope
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.rx2.openSubscription


abstract class SmartVDC : VDC() {
    private val vdcSubs = CompositeDisposable()

    fun addVdcDisp(disposable: Disposable) {
        vdcSubs.add(disposable)
    }

    inline fun <T> consumeScopeVDC(
            source: Observable<T>,
            crossinline consumer: suspend (value: T) -> Unit
    ): Job = viewModelScope.launch(Dispatchers.Default) {
        @UseExperimental(ObsoleteCoroutinesApi::class)
        val sub = source.openSubscription()
        @UseExperimental(FlowPreview::class)
        sub.consumeAsFlow().collect { consumer.invoke(it) }
    }

    override fun onCleared() {
        vdcSubs.dispose()
        super.onCleared()
    }
}