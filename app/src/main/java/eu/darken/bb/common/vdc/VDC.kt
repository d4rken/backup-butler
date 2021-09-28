package eu.darken.bb.common.vdc

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import eu.darken.bb.common.debug.logging.logTag
import timber.log.Timber

abstract class VDC : ViewModel() {
    val tag: String = logTag("VDC", javaClass.simpleName)

    init {
        Timber.tag(tag).v("Initialized")
    }

    @CallSuper
    override fun onCleared() {
        Timber.tag(tag).v("onCleared()")
        super.onCleared()
    }
}