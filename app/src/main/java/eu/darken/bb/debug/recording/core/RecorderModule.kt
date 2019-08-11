package eu.darken.bb.debug.recording.core

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.BuildConfig
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.startServiceCompat
import eu.darken.bb.debug.DebugModule
import eu.darken.bb.debug.recording.ui.RecorderActivity
import eu.thedarken.sdm.tools.debug.DebugModuleHost
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File

class RecorderModule @AssistedInject constructor(
        @Assisted private val host: DebugModuleHost,
        @AppContext private val context: Context
) : DebugModule {

    private var recorder: Recorder? = null

    init {
        host.observeOptions().subscribeOn(Schedulers.io())
                .subscribe { options ->
                    if (recorder == null && options.isRecording) {
                        recorder = Recorder()

                        var path = options.recorderPath
                        if (path == null) path = createRecordingFilePath()

                        recorder!!.start(File(path))
                        host.getSettings().edit().putString(KEY_RECORDER_PATH, path).apply()
                        host.submit(options.copy(recorderPath = path, level = Log.VERBOSE))

                        context.startServiceCompat(Intent(context, RecorderService::class.java))
                    } else if (recorder != null && !options.isRecording) {
                        recorder!!.stop()
                        recorder = null
                        val intent = RecorderActivity.getLaunchIntent(context, options.recorderPath!!)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        host.getSettings().edit().remove(KEY_RECORDER_PATH).apply()
                        host.submit(options.copy(recorderPath = null))
                    }
                }

        var recorderPath: String? = null
        if (host.getSettings().contains(KEY_RECORDER_PATH)) {
            recorderPath = host.getSettings().getString(KEY_RECORDER_PATH, null)
        }

        if (recorderPath == null) {
            val triggerFile = try {
                File(context.getExternalFilesDir(null), FORCE_FILE)
            } catch (e: Exception) {
                File(Environment.getExternalStorageDirectory(), "/Android/data/${BuildConfig.APPLICATION_ID}/files/$FORCE_FILE")
            }
            if (triggerFile.exists()) {
                if (!triggerFile.delete()) Timber.tag(TAG).w("Failed to consume trigger file")
                recorderPath = createRecordingFilePath()
            }
        }

        if (recorderPath != null) {
            host.observeOptions().firstOrError()
                    .subscribe { debugOptions ->
                        host.submit(debugOptions.copy(isRecording = true, recorderPath = recorderPath))
                    }
        }
    }

    private fun createRecordingFilePath(): String {
        return File(File(context.externalCacheDir, "logfiles"), "bb_logfile_" + System.currentTimeMillis() + ".txt").path
    }

    @AssistedInject.Factory
    interface Factory : DebugModule.Factory<RecorderModule>

    companion object {
        internal val TAG = App.logTag("Debug", "RecorderModule")
        private const val KEY_RECORDER_PATH = "debug.recorder.path"
        private const val FORCE_FILE = "bb_force_debug_run"
    }
}