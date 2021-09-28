package eu.darken.bb.main.ui.advanced.debug

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.getCompatColor
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.DebugFragmentBinding

@AndroidEntryPoint
class DebugFragment : SmartFragment(R.layout.debug_fragment) {
    private val vdc: DebugFragmentVDC by viewModels()
    private val ui: DebugFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.rootResult.observe2(this, ui) {
            rootTestLoadingOverlay.isInvisible = !it.isWorking
            rootTestContainer.isInvisible = it.isWorking
            rootTestOutput.text = it.output
            rootTestOutput.setTextColor(
                when (it.result) {
                    1 -> getCompatColor(R.color.colorSecondary)
                    -1 -> getCompatColor(R.color.colorError)
                    else -> getColorForAttr(R.attr.colorOnBackground)
                }
            )
        }
        ui.apply {
            rootTestJavaAction.clicksDebounced().subscribe { vdc.performJavaRootCheck() }
            rootTestShellAction.clicksDebounced().subscribe { vdc.performShellRootCheck() }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        val TAG = logTag("Debug", "Fragment")
    }
}
