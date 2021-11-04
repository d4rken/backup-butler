package eu.darken.bb.backup.ui.generator.picker

import android.os.Bundle
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.smart.SmartFragment

interface GeneratorPickerListener {

    fun SmartFragment.setupGeneratorPickerListener(
        callback: (GeneratorPickerResult?) -> Unit
    ) {
        val fragment = this
        log { "setupGeneratorPickerListener(...) on ${fragment.hashCode()}" }
        setFragmentResultListener(RESULT_KEY) { key, bundle ->
            log { "setupGeneratorPickerListener() on ${fragment.hashCode()} -> $key - $bundle" }
            callback(bundle.getParcelable(RESULT_KEY))
        }
    }

    companion object {
        internal const val RESULT_KEY = "GeneratorPickerResult"
    }
}


fun GeneratorPickerFragment.setGeneratorPickerResult(
    result: GeneratorPickerResult?
) {
    log { "setGeneratorPickerResult(result=$result)" }
    setFragmentResult(
        GeneratorPickerListener.RESULT_KEY,
        Bundle().apply {
            putParcelable(GeneratorPickerListener.RESULT_KEY, result)
        }
    )
}