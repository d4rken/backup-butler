package eu.darken.bb.main.ui

import android.content.Intent
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
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.onboarding.OnboardingActivity

@AndroidEntryPoint
class InitFragment : SmartFragment(R.layout.init_fragment) {

    private val vdc: InitFragmentVDC by viewModels()
    private val binding: InitFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.launchConfig.observe2(this) {
            log { "Executing launchConfig=$it" }
            if (it.showOnboarding) {
                startActivity(Intent(requireContext(), OnboardingActivity::class.java))
            } else {
                when (it.startMode) {
                    UISettings.StartMode.SIMPLE -> InitFragmentDirections.actionInitFragmentToSimpleModeFragment()
                    UISettings.StartMode.ADVANCED -> InitFragmentDirections.actionInitFragmentToAdvancedModeFragment()
                }.run { doNavigate(this) }
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
