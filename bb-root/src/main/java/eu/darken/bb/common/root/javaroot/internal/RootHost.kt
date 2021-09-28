package eu.darken.bb.common.root.javaroot.internal

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import eu.darken.bb.common.debug.logging.Logging.Priority.ERROR
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log

@SuppressLint("PrivateApi")
abstract class RootHost {

    /**
     * Retrieve system context<br></br>
     * <br></br>
     * Stability: unlikely to change, this implementation works from 1.6 through 9.0<br></br>
     *
     * @return system context
     */
    val systemContext: Context by lazy {
        try {
            // a prepared Looper is required for the calls below to succeed
            if (Looper.getMainLooper() == null) {
                try {
                    Looper.prepareMainLooper()
                } catch (e: Exception) {
                    log(ERROR) { "Failed prepareMainLooper() for systemContext" }
                }
            }
            val cActivityThread = Class.forName("android.app.ActivityThread")
            val mSystemMain = cActivityThread.getMethod("systemMain")
            val mGetSystemContext = cActivityThread.getMethod("getSystemContext")
            val oActivityThread = mSystemMain.invoke(null)
            val oContext = mGetSystemContext.invoke(oActivityThread)
            log { "Grabbed context $oContext" }
            oContext as Context
        } catch (e: Exception) {
            log(ERROR) { "Failed to obtain system context: ${e.asLog()}" }
            throw RuntimeException("Unexpected exception in getSystemContext()")
        }
    }
}