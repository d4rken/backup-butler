package eu.darken.bb.common

import android.content.Context
import eu.darken.bb.R

class OpStatus(override val context: Context) : HasContext {

    var success = 0
    var skipped = 0
    var failed = 0
    var forceDisplay = false

    fun toDisplayString(): String {
        val result = StringBuilder()
        if (success != 0 || forceDisplay) {
            result.append(getString(R.string.result_x_successful, success))
        }
        if (skipped != 0 || forceDisplay) {
            if (success != 0 || forceDisplay) result.append(" | ")
            result.append(getString(R.string.result_x_skipped, skipped))
        }
        if (failed != 0 || forceDisplay) {
            if (success != 0 || skipped != 0 || forceDisplay) result.append(" | ")
            result.append(getString(R.string.result_x_failed, failed))
        }
        return result.toString()
    }

}