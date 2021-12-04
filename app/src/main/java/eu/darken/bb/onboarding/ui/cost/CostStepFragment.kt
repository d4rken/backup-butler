package eu.darken.bb.onboarding.ui.cost

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.OnboardingStepCostFragmentBinding
import eu.darken.bb.main.core.UISettings
import javax.inject.Inject

@AndroidEntryPoint
class CostStepFragment : Smart2Fragment(R.layout.onboarding_step_cost_fragment) {

    override val vdc: CostStepFragmentVDC by viewModels()
    override val ui: OnboardingStepCostFragmentBinding by viewBinding()

    @Inject lateinit var uiSettings: UISettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.continueAction.setOnClickListener { vdc.onContinue() }

        super.onViewCreated(view, savedInstanceState)
    }
}
