package eu.darken.bb.common.root.javaroot.internal

import android.annotation.SuppressLint
import android.content.Context
import android.os.Debug
import android.os.Looper
import android.util.Base64
import android.util.Log
import eu.darken.bb.common.debug.logging.LogCatLogger
import eu.darken.bb.common.debug.logging.Logging
import eu.darken.bb.common.debug.logging.Logging.Priority.ERROR
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.parcel.unmarshall
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.reflect.Method
import kotlin.system.exitProcess

@SuppressLint("PrivateApi")
abstract class RootHost(
    private val tag: String,
    private val _args: List<String>
) {

    val rootHostScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var options: RootHostOptions

    val ourPkgName: String
        get() = options.packageName
    val pairingCode: String
        get() = options.pairingCode
    val isDebug: Boolean
        get() = options.isDebug

    @SuppressLint("LogNotTimber")
    fun start() = try {
        Log.d(tag, "start(): RootHost args=${_args}")

        val optionsBase64 = _args.single().let {
            require(it.startsWith("$OPTIONS_KEY=")) { "Unexpected options format: $_args" }
            it.removePrefix("$OPTIONS_KEY=")
        }

        Log.d(tag, "start(): unmarshalling $optionsBase64")
        options = Base64.decode(optionsBase64, 0).unmarshall()
        Log.d(tag, "start(): options=$options")

        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            log(tag, ERROR) { "Uncaught exception within JavaRootHost: ${throwable.asLog()}" }
            if (oldHandler != null) oldHandler.uncaughtException(thread, throwable)
            else exitProcess(1)
        }

        if (options.isDebug) {
            Logging.install(LogCatLogger())
            Timber.plant(Timber.DebugTree())

            setAppName("$ourPkgName:rootHost")

            while (options.waitForDebugger && !Debug.isDebuggerConnected()) {
                try {
                    Thread.sleep(200)
                } catch (ignored: InterruptedException) {
                }
            }
        }

        runBlocking {
            onInit()
            onExecute()
        }
    } catch (e: Throwable) {
        Log.e(tag, "Failed to run RootHost.", e)
        throw e
    } finally {
        rootHostScope.cancel()
        Log.v(tag, "start() RootHost finished")
    }

    abstract suspend fun onInit()

    abstract suspend fun onExecute()

    @SuppressLint("PrivateApi,DiscouragedPrivateApi")
    private fun setAppName(name: String?) = try {
        log(tag) { "Setting appName=$name" }
        val ddm = Class.forName("android.ddm.DdmHandleAppName")
        val m: Method = ddm.getDeclaredMethod("setAppName", String::class.java, Int::class.javaPrimitiveType)
        m.invoke(null, name, 0)
    } catch (e: Exception) {
        throw RuntimeException(e)
    }

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

    companion object {
        const val OPTIONS_KEY = "ROOT_HOST_OPTIONS"
    }
}