package eu.darken.bb.common.debug.recording.core

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.BuildConfig
import eu.darken.bb.common.debug.DebugModule
import eu.darken.bb.common.debug.DebugModuleHost
import eu.darken.bb.common.debug.DebugScope
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.debug.recording.ui.RecorderActivity
import eu.darken.bb.common.startServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.io.File

class RecorderModule @AssistedInject constructor(
    @Assisted private val host: DebugModuleHost,
    @ApplicationContext private val context: Context,
    @DebugScope private val debugScope: CoroutineScope,
) : DebugModule {

    private var recorder: Recorder? = null

    init {
        host.observeOptions()
            .onEach { options ->
                log(TAG) { "New options: $options" }
                if (recorder == null && options.isRecording) {
                    recorder = Recorder()

                    var path = options.recorderPath
                    if (path == null) path = createRecordingFilePath()

                    recorder!!.start(File(path))
                    host.getSettings().edit().putString(KEY_RECORDER_PATH, path).apply()
                    host.submit { it.copy(recorderPath = path, level = Log.VERBOSE) }

                    context.startServiceCompat(Intent(context, RecorderService::class.java))
                } else if (recorder != null && !options.isRecording) {
                    recorder!!.stop()
                    recorder = null
                    val intent = RecorderActivity.getLaunchIntent(context, options.recorderPath!!)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    host.getSettings().edit().remove(KEY_RECORDER_PATH).apply()
                    host.submit { it.copy(recorderPath = null) }
                }
            }
            .launchIn(debugScope)

        var recorderPath: String? = null
        if (host.getSettings().contains(KEY_RECORDER_PATH)) {
            recorderPath = host.getSettings().getString(KEY_RECORDER_PATH, null)
        }

        if (recorderPath == null) {
            val triggerFile = try {
                File(context.getExternalFilesDir(null), FORCE_FILE)
            } catch (e: Exception) {
                File(
                    Environment.getExternalStorageDirectory(),
                    "/Android/data/${BuildConfig.APPLICATION_ID}/files/$FORCE_FILE"
                )
            }
            if (triggerFile.exists()) {
                if (!triggerFile.delete()) Timber.tag(TAG).w("Failed to consume trigger file")
                recorderPath = createRecordingFilePath()
            }
        }

        if (recorderPath != null) {
            host.submit { it.copy(isRecording = true, recorderPath = recorderPath) }
        }
    }

    private fun createRecordingFilePath(): String {
        return File(
            File(context.externalCacheDir, "logfiles"),
            "bb_logfile_" + System.currentTimeMillis() + ".txt"
        ).path
    }

    @AssistedFactory
    interface Factory : DebugModule.Factory<RecorderModule>

    companion object {
        internal val TAG = logTag("Debug", "RecorderModule")
        private const val KEY_RECORDER_PATH = "debug.recorder.path"
        private const val FORCE_FILE = "bb_force_debug_run"
    }
}