package eu.darken.bb.main.ui.onboarding.beta

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.OnboardingStepBetaFragmentBinding
import eu.darken.bb.main.core.UISettings
import javax.inject.Inject

@AndroidEntryPoint
class BetaStepFragment : SmartFragment(R.layout.onboarding_step_beta_fragment) {

    private val vdc: BetaStepFragmentVDC by viewModels()
    private val ui: OnboardingStepBetaFragmentBinding by viewBinding()

    @Inject lateinit var uiSettings: UISettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        vdc.navEvents.observe2(this) { doNavigate(it) }

        ui.continueAction.setOnClickListener { vdc.onContinue() }

        super.onViewCreated(view, savedInstanceState)
    }
}
