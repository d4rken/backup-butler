package eu.darken.bb.onboarding.steps.hello

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import butterknife.BindView
import dagger.android.support.AndroidSupportInjection
import eu.darken.bb.R
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.main.ui.MainActivity
import javax.inject.Inject


class HelloStepFragment : SmartFragment() {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: HelloStepFragmentVDC by vdcs { vdcSource }

    @BindView(R.id.action_start_simplemode) lateinit var actionStartSimple: Button
    @BindView(R.id.action_start_advancedmode) lateinit var actionStartAdvanced: Button

    @Inject lateinit var uiSettings: UISettings

    init {
        layoutRes = R.layout.onboarding_hello_step_fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
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
