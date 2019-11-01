package eu.darken.bb.common.debug

import android.util.Log

data class DebugOptions(
        val level: Int,
        val isRecording: Boolean,
        val recorderPath: String?
) {
    fun isDebug(): Boolean {
        return level <= Log.DEBUG
    }

    companion object {
        fun default() = DebugOptions(
                level = Log.WARN,
                isRecording = false,
                recorderPath = null
        )
    }
}

fun DebugOptions.compareIgnorePath(target: DebugOptions): Boolean {
    return copy(recorderPath = null) == target.copy(recorderPath = null)
}