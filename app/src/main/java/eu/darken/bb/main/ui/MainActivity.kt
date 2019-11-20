package eu.darken.bb.main.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import eu.darken.bb.App
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.main.ui.advanced.AdvancedActivity
import eu.darken.bb.main.ui.simple.SimpleActivity
import eu.darken.bb.onboarding.OnboardingActivity
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject lateinit var uiSettings: UISettings

    override fun onCreate(savedInstanceState: Bundle?) {
        App.instance.appComponent.inject(this)
        super.onCreate(savedInstanceState)

        if (uiSettings.showOnboarding) {
            startActivity(Intent(this, OnboardingActivity::class.java))
        } else {
            when (uiSettings.startMode) {
                UISettings.StartMode.SIMPLE -> {
                    startActivity(Intent(this, SimpleActivity::class.java))
                }
                UISettings.StartMode.ADVANCED -> {
                    startActivity(Intent(this, AdvancedActivity::class.java))
                }
            }
        }

        finish()
    }
}