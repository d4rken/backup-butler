package eu.darken.bb.onboarding

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import butterknife.ButterKnife
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    private val vdc: OnboardingActivityVDC by viewModels()

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
