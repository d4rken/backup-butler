package eu.darken.bb.storage.ui.editor

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.smart.SmartFragment


interface StorageEditorResultListener {

    fun SmartFragment.setupStorageEditorListener(
        callback: (StorageEditorResult) -> Unit
    ) {
        val fragment = this
        log { "setupStorageEditorListener(...) on ${fragment.hashCode()}" }
        setFragmentResultListener(RESULT_KEY) { key, bundle ->
            log { "setupStorageEditorListener() on ${fragment.hashCode()} -> $key - $bundle" }
            callback(bundle.getParcelable(RESULT_KEY)!!)
        }
    }

    companion object {
        internal const val RESULT_KEY = "StorageEditorResult"
    }
}


fun <T> T.setStorageEditorResult(result: StorageEditorResult?) where T : EditorFragmentChild, T : Fragment {
    log { "setStorageEditorResult(result=$result)" }
    setFragmentResult(
        StorageEditorResultListener.RESULT_KEY,
        Bundle().apply {
            putParcelable(StorageEditorResultListener.RESULT_KEY, result)
        }
    )
}
