package eu.darken.bb.onboarding.ui.cost

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.OnboardingStepCostFragmentBinding
import eu.darken.bb.main.core.UISettings
import javax.inject.Inject

@AndroidEntryPoint
class CostStepFragment : SmartFragment(R.layout.onboarding_step_cost_fragment) {

    private val vdc: CostStepFragmentVDC by viewModels()
    private val ui: OnboardingStepCostFragmentBinding by viewBinding()

    @Inject lateinit var uiSettings: UISettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        vdc.navEvents.observe2(this) { doNavigate(it) }

        ui.continueAction.setOnClickListener { vdc.onContinue() }

        super.onViewCreated(view, savedInstanceState)
    }
}
