package eu.darken.bb.processor.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.processor.ui.progress.ProgressFragment

@AndroidEntryPoint
class ProcessorActivity : AppCompatActivity() {

    private val vdc: ProcessorActivityVDC by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.processor_activity)
        ButterKnife.bind(this)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, ProgressFragment())
                .commit()
        }
    }

}
