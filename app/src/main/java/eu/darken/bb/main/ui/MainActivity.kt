package eu.darken.bb.main.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import eu.darken.bb.App
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.main.ui.advanced.AdvancedActivity
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject lateinit var uiSettings: UISettings

    override fun onCreate(savedInstanceState: Bundle?) {
        App.instance.appComponent.inject(this)
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, AdvancedActivity::class.java))
        finish()
    }
}