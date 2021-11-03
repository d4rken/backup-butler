package eu.darken.bb.common.files.ui.picker

import androidx.lifecycle.ViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.storage.core.Storage
import timber.log.Timber

class SharedPathPickerVM
    : ViewModel() {

    val resultEvent = SingleLiveEvent<PathPicker.Result>()
    val typeEvent = SingleLiveEvent<Storage.Type>()

    init {
        Timber.tag(TAG).d("Init: %s", this)
    }

    fun postResult(result: PathPicker.Result) {
        resultEvent.postValue(result)
    }

    fun launchType(type: Storage.Type) {
        typeEvent.postValue(type)
    }

    companion object {
        private val TAG = logTag("Picker", "SharedPickerVM")
    }
}