package eu.darken.bb.onboarding.ui.mode

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.OnboardingStepModeFragmentBinding
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.main.ui.MainActivity
import javax.inject.Inject

@AndroidEntryPoint
class ModeStepFragment : SmartFragment(R.layout.onboarding_step_mode_fragment) {

    private val vdc: ModeStepFragmentVDC by viewModels()
    private val ui: OnboardingStepModeFragmentBinding by viewBinding()

    @Inject lateinit var uiSettings: UISettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        ui.modeSimpleAction.clicksDebounced().subscribe {
            vdc.onSimpleSelected()
        }
        ui.modeAdvancedAction.clicksDebounced().subscribe {
            vdc.onAdvancedSelected()
        }

        vdc.finishOnboardingEvent.observe2(this) {
            startActivity(Intent(requireActivity(), MainActivity::class.java))
            requireActivity().finish()
        }

        super.onViewCreated(view, savedInstanceState)
    }


}
