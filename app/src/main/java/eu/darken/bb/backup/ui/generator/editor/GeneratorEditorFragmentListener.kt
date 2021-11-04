package eu.darken.bb.backup.ui.generator.editor

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.smart.SmartFragment


interface GeneratorEditorResultListener {

    fun SmartFragment.setupGeneratorEditorListener(
        callback: (GeneratorEditorResult) -> Unit
    ) {
        val fragment = this
        log { "setupGeneratorEditorListener(...) on ${fragment.hashCode()}" }
        setFragmentResultListener(RESULT_KEY) { key, bundle ->
            log { "setupGeneratorEditorListener() on ${fragment.hashCode()} -> $key - $bundle" }
            callback(bundle.getParcelable(RESULT_KEY)!!)
        }
    }

    companion object {
        internal const val RESULT_KEY = "GeneratorEditorResult"
    }
}


fun <T> T.setGeneratorEditorResult(result: GeneratorEditorResult?) where T : GeneratorEditorFragmentChild, T : Fragment {
    log { "setGeneratorEditorResult(result=$result)" }
    setFragmentResult(
        GeneratorEditorResultListener.RESULT_KEY,
        Bundle().apply {
            putParcelable(GeneratorEditorResultListener.RESULT_KEY, result)
        }
    )
}
