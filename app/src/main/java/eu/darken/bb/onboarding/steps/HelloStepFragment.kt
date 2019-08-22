package eu.darken.bb.onboarding.steps

import android.os.Bundle
import dagger.android.support.AndroidSupportInjection
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import javax.inject.Inject


class HelloStepFragment : SmartFragment() {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: HelloStepFragmentVDC by vdcs { vdcSource }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    init {
        layoutRes = R.layout.onboarding_step_fragment
    }

}
