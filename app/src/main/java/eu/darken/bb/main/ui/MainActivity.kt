package eu.darken.bb.main.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.navigation.findNavController
import eu.darken.bb.databinding.MainActivityBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val vdc: MainActivityVDC by viewModels()
    private lateinit var ui: MainActivityBinding
    private val navController by lazy { supportFragmentManager.findNavController(R.id.nav_host) }

    var showSplashScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
        splashScreen.setKeepVisibleCondition { showSplashScreen && savedInstanceState == null }

        ui = MainActivityBinding.inflate(layoutInflater)
        setContentView(ui.root)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(B_KEY_SPLASH, showSplashScreen)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val B_KEY_SPLASH = "showSplashScreen"
    }
}