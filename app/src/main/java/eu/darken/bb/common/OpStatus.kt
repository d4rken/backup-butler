package eu.darken.bb.common

import android.content.Context
import eu.darken.bb.R

data class OpStatus(
    var success: Int = 0,
    var skipped: Int = 0,
    var failed: Int = 0,
    var forceDisplay: Boolean = false
) {

    fun toDisplayString(context: Context): String {
        val result = StringBuilder()
        if (success != 0 || forceDisplay) {
            result.append(context.getString(R.string.result_x_successful, success))
        }
        if (skipped != 0 || forceDisplay) {
            if (success != 0 || forceDisplay) result.append(" | ")
            result.append(context.getString(R.string.result_x_skipped, skipped))
        }
        if (failed != 0 || forceDisplay) {
            if (success != 0 || skipped != 0 || forceDisplay) result.append(" | ")
            result.append(context.getString(R.string.result_x_failed, failed))
        }
        return result.toString()
    }

}