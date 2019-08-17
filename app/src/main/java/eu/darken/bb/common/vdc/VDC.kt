package eu.darken.bb.common.vdc

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import eu.darken.bb.App
import timber.log.Timber

abstract class VDC : ViewModel() {
    val tag: String = App.logTag("VDC", javaClass.simpleName)

    init {
        Timber.tag(tag).v("Initialized")
    }

    @CallSuper
    override fun onCleared() {
        Timber.tag(tag).v("onCleared()")
        super.onCleared()
    }
}