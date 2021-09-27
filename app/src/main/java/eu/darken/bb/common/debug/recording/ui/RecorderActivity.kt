package eu.darken.bb.common.debug.recording.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.App
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartActivity
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.common.ui.setInvisible
import eu.darken.bb.databinding.CoreDebugRecordingActivityBinding

@AndroidEntryPoint
class RecorderActivity : SmartActivity() {

    private lateinit var ui: CoreDebugRecordingActivityBinding
    private val vdc: RecorderActivityVDC by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = CoreDebugRecordingActivityBinding.inflate(layoutInflater)
        setContentView(ui.root)

        vdc.state.observe2(this) { state ->
            ui.loadingIndicator.setInvisible(!state.loading)
            ui.share.setInvisible(state.loading)

            ui.recordingPath.text = state.normalPath

            if (state.normalSize != -1L) {
                ui.recordingSize.text = Formatter.formatShortFileSize(this, state.normalSize)
            }
            if (state.compressedSize != -1L) {
                ui.recordingSizeCompressed.text = Formatter.formatShortFileSize(this, state.compressedSize)
            }

            if (state.error != null) {
                Toast.makeText(this, state.error.tryLocalizedErrorMessage(this), Toast.LENGTH_LONG).show()
            }
        }

        ui.share.clicksDebounced().subscribe { vdc.share() }
    }

    companion object {
        internal val TAG = App.logTag("RecorderActivity")
        const val RECORD_PATH = "originalExclusion"

        fun getLaunchIntent(context: Context, path: String): Intent {
            val intent = Intent(context, RecorderActivity::class.java)
            intent.putExtra(RECORD_PATH, path)
            return intent
        }
    }
}
