package eu.darken.bb.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import butterknife.ButterKnife
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

//    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
//    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: OnboardingActivityVDC by vdcs { vdcSource }

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_onboarding_activity)
        ButterKnife.bind(this)

        val graph = navController.navInflater.inflate(R.navigation.onboarding)
        navController.graph = graph
    }

    // https://github.com/AppIntro/AppIntro
    // https://github.com/worker8/TourGuide
    // https://github.com/deano2390/MaterialShowcaseView
}
