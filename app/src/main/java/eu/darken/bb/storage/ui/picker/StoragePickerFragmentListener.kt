package eu.darken.bb.storage.ui.picker

import android.os.Bundle
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.smart.SmartFragment

interface StoragePickerListener {

    fun SmartFragment.setupStoragePickerListener(
        callback: (StoragePickerResult?) -> Unit
    ) {
        val fragment = this
        log { "setupStoragePickerListener(...) on ${fragment.hashCode()}" }
        setFragmentResultListener(RESULT_KEY) { key, bundle ->
            log { "setupStoragePickerListener() on ${fragment.hashCode()} -> $key - $bundle" }
            callback(bundle.getParcelable(RESULT_KEY))
        }
    }

    companion object {
        internal const val RESULT_KEY = "StoragePickerResult"
    }
}


fun StoragePickerFragment.setStoragePickerResult(
    result: StoragePickerResult?
) {
    log { "setStoragePickerResult(result=$result)" }
    setFragmentResult(
        StoragePickerListener.RESULT_KEY,
        Bundle().apply {
            putParcelable(StoragePickerListener.RESULT_KEY, result)
        }
    )
}