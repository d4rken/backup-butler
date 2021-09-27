package eu.darken.bb.processor.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.databinding.ProcessorActivityBinding
import eu.darken.bb.processor.ui.progress.ProgressFragment

@AndroidEntryPoint
class ProcessorActivity : AppCompatActivity() {

    private val vdc: ProcessorActivityVDC by viewModels()
    private lateinit var ui: ProcessorActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = ProcessorActivityBinding.inflate(layoutInflater)
        setContentView(ui.root)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, ProgressFragment())
                .commit()
        }
    }

}
