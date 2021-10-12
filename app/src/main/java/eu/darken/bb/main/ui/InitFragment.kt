package eu.darken.bb.main.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.InitFragmentBinding

@AndroidEntryPoint
class InitFragment : SmartFragment(R.layout.init_fragment) {

    private val vdc: InitFragmentVDC by viewModels()
    private val binding: InitFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.navEvents.observe2(this) {
            log { "Executing navEvent=$it" }
            doNavigate(it)
        }
        vdc.finishSplashScreen.observe2(this) {
            (requireActivity() as MainActivity).showSplashScreen = false
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
