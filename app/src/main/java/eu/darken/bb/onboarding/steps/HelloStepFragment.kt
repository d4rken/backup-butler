package eu.darken.bb.onboarding.steps

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import butterknife.ButterKnife
import dagger.android.support.AndroidSupportInjection
import eu.darken.androidkotlinstarter.common.dagger.VDCSource
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartFragment
import javax.inject.Inject


class HelloStepFragment : SmartFragment() {
    companion object {
        fun newInstance(): Fragment = HelloStepFragment()
    }

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val viewModel: HelloStepFragmentViewModel by viewModels { vdcSource.create(this, null) }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.onboarding_step_fragment, container, false)
        addUnbinder(ButterKnife.bind(this, layout))
        return layout
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
