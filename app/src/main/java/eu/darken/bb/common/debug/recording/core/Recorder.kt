package eu.darken.bb.common.debug.recording.core

import eu.darken.bb.App
import eu.darken.bb.common.debug.logging.FileLogger
import eu.darken.bb.common.debug.logging.Logging
import timber.log.Timber
import java.io.File

class Recorder {
    private var fileLogger: FileLogger? = null

    var path: File? = null
        private set

    fun start(path: File) {
        if (fileLogger != null) return
        this.path = path
        fileLogger = FileLogger(path)
        fileLogger?.let {
            it.start()
            Logging.install(it)
            Timber.i("Now logging to file!")
        }
    }

    fun stop() {
        fileLogger?.let {
            Timber.i("Stopping file-logger-tree: $it")
            Logging.remove(it)
            it.stop()
            fileLogger = null
            this.path = null
        }
    }

    fun isRecording(): Boolean = fileLogger != null

    companion object {
        internal val TAG = App.logTag("Recorder")
    }

}