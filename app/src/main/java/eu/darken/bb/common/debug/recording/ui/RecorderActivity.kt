package eu.darken.bb.common.debug.recording.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.Formatter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import butterknife.BindView
import butterknife.ButterKnife
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartActivity
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.common.ui.setInvisible

@AndroidEntryPoint
class RecorderActivity : SmartActivity() {

    @BindView(R.id.recording_path) lateinit var recordingPath: TextView
    @BindView(R.id.recording_size) lateinit var recordingSize: TextView
    @BindView(R.id.recording_size_compressed) lateinit var recordingCompressedSize: TextView
    @BindView(R.id.loading_indicator) lateinit var loadingIndicator: ProgressBar
    @BindView(R.id.share) lateinit var share: Button

    private val vdc: RecorderActivityVDC by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.core_debug_recording_activity)
        ButterKnife.bind(this)

        vdc.state.observe(this, Observer { state ->
            loadingIndicator.setInvisible(!state.loading)
            share.setInvisible(state.loading)

            recordingPath.text = state.normalPath

            if (state.normalSize != -1L) {
                recordingSize.text = Formatter.formatShortFileSize(this, state.normalSize)
            }
            if (state.compressedSize != -1L) {
                recordingCompressedSize.text = Formatter.formatShortFileSize(this, state.compressedSize)
            }

            if (state.error != null) {
                Toast.makeText(this, state.error.tryLocalizedErrorMessage(this), Toast.LENGTH_LONG).show()
            }
        })

        share.clicksDebounced().subscribe { vdc.share() }
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
