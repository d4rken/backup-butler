package eu.darken.bb.onboarding

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.databinding.MainOnboardingActivityBinding

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    private val vdc: OnboardingActivityVDC by viewModels()
    private lateinit var ui: MainOnboardingActivityBinding
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_onboarding_activity)
        ui = MainOnboardingActivityBinding.inflate(layoutInflater)

        val graph = navController.navInflater.inflate(R.navigation.onboarding)
        navController.graph = graph
    }

    // https://github.com/AppIntro/AppIntro
    // https://github.com/worker8/TourGuide
    // https://github.com/deano2390/MaterialShowcaseView
}
