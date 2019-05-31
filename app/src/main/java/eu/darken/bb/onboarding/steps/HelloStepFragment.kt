package eu.darken.bb.onboarding.steps

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import butterknife.ButterKnife
import dagger.android.support.AndroidSupportInjection
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartFragment
import javax.inject.Inject


class HelloStepFragment : SmartFragment() {
    companion object {
        fun newInstance(): Fragment = HelloStepFragment()
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: HelloStepFragmentViewModel

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
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HelloStepFragmentViewModel::class.java)
        super.onViewCreated(view, savedInstanceState)
    }
}
