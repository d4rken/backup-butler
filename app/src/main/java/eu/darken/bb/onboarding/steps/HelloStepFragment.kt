package eu.darken.bb.onboarding.steps

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dagger.android.support.AndroidSupportInjection
import eu.darken.bb.R
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcs
import javax.inject.Inject


class HelloStepFragment : SmartFragment() {
    companion object {
        fun newInstance(): Fragment = HelloStepFragment()
    }

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

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
