package eu.darken.bb.onboarding.steps.hello

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.OnboardingHelloStepFragmentBinding
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.main.ui.MainActivity
import javax.inject.Inject

@AndroidEntryPoint
class HelloStepFragment : SmartFragment(R.layout.onboarding_hello_step_fragment) {

    private val vdc: HelloStepFragmentVDC by viewModels()
    private val ui: OnboardingHelloStepFragmentBinding by viewBinding()

    @Inject lateinit var uiSettings: UISettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        ui.actionStartSimplemode.clicksDebounced().subscribe {
            uiSettings.showOnboarding = false
            uiSettings.startMode = UISettings.StartMode.SIMPLE
            startActivity(Intent(requireActivity(), MainActivity::class.java))
            finishActivity()
        }
        ui.actionStartAdvancedmode.clicksDebounced().subscribe {
            uiSettings.showOnboarding = false
            uiSettings.startMode = UISettings.StartMode.ADVANCED
            startActivity(Intent(requireActivity(), MainActivity::class.java))
            finishActivity()
        }

        super.onViewCreated(view, savedInstanceState)
    }


}
