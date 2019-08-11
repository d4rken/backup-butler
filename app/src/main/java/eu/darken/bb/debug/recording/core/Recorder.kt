package eu.darken.bb.debug.recording.core

import eu.darken.bb.App
import eu.darken.bb.common.timber.FileLoggerTree
import timber.log.Timber
import java.io.File

class Recorder {
    private var fileLoggerTree: FileLoggerTree? = null

    var path: File? = null
        private set

    fun start(path: File) {
        if (fileLoggerTree != null) return
        this.path = path
        fileLoggerTree = FileLoggerTree(path)
        fileLoggerTree?.let {
            it.start()
            Timber.plant(it)
            Timber.tag(TAG).i("Now logging to file!")
        }
    }

    fun stop() {
        fileLoggerTree?.let {
            Timber.tag(TAG).i("Stopping file-logger-tree: $it")
            Timber.uproot(it)
            it.stop()
            fileLoggerTree = null
            this.path = null
        }
    }

    fun isRecording(): Boolean = fileLoggerTree != null

    companion object {
        internal val TAG = App.logTag("Recorder")
    }

}