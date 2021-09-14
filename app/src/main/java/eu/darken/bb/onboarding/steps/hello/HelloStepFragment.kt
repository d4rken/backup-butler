package eu.darken.bb.onboarding.steps.hello

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.viewModels
import butterknife.BindView
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.main.ui.MainActivity
import javax.inject.Inject

@AndroidEntryPoint
class HelloStepFragment : SmartFragment(R.layout.onboarding_hello_step_fragment) {

    private val vdc: HelloStepFragmentVDC by viewModels()

    @BindView(R.id.action_start_simplemode) lateinit var actionStartSimple: Button
    @BindView(R.id.action_start_advancedmode) lateinit var actionStartAdvanced: Button

    @Inject lateinit var uiSettings: UISettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        actionStartSimple.clicksDebounced().subscribe {
            uiSettings.showOnboarding = false
            uiSettings.startMode = UISettings.StartMode.SIMPLE
            startActivity(Intent(requireActivity(), MainActivity::class.java))
            finishActivity()
        }
        actionStartAdvanced.clicksDebounced().subscribe {
            uiSettings.showOnboarding = false
            uiSettings.startMode = UISettings.StartMode.ADVANCED
            startActivity(Intent(requireActivity(), MainActivity::class.java))
            finishActivity()
        }

        super.onViewCreated(view, savedInstanceState)
    }


}
