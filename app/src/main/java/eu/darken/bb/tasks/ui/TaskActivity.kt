package eu.darken.bb.tasks.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.vdcs
import eu.darken.bb.main.ui.overview.OverviewFragment
import eu.darken.bb.onboarding.OnboardingActivityVDC
import javax.inject.Inject


class TaskActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: TaskActivityVDC by vdcs { vdcSource }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.BaseAppTheme_NoActionBar)
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.task_activity)
        ButterKnife.bind(this)

        vdc.state.observe(this, Observer { })
    }

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    private fun showStep(step: OnboardingActivityVDC.State.Step) {
        var fragment = supportFragmentManager.findFragmentById(R.id.content_frame)
        if (fragment == null) fragment = OverviewFragment.newInstance()
        supportFragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commitAllowingStateLoss()
    }
}
