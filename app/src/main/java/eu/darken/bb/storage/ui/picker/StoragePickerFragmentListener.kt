package eu.darken.bb.storage.ui.picker

import android.os.Bundle
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.smart.SmartFragment

interface StoragePickerResultListener {

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
        StoragePickerResultListener.RESULT_KEY,
        Bundle().apply {
            putParcelable(StoragePickerResultListener.RESULT_KEY, result)
        }
    )
}