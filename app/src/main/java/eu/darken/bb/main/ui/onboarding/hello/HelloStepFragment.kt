package eu.darken.bb.main.ui.onboarding.hello

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.OnboardingStepHelloFragmentBinding
import eu.darken.bb.main.core.UISettings
import javax.inject.Inject

@AndroidEntryPoint
class HelloStepFragment : SmartFragment(R.layout.onboarding_step_hello_fragment) {

    private val vdc: HelloStepFragmentVDC by viewModels()
    private val ui: OnboardingStepHelloFragmentBinding by viewBinding()

    @Inject lateinit var uiSettings: UISettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        ui.continueAction.setOnClickListener { vdc.onContinue() }

        vdc.navEvent.observe2(this) { doNavigate(it) }

        super.onViewCreated(view, savedInstanceState)
    }

}
