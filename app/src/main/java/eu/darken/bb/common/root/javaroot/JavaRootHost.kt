package eu.darken.bb.common.root.javaroot

import android.annotation.SuppressLint
import android.util.Log
import dagger.Lazy
import eu.darken.bb.BuildConfig
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.debug.logging.*
import eu.darken.bb.common.debug.logging.Logging.Priority.ERROR
import eu.darken.bb.common.debug.logging.Logging.Priority.INFO
import eu.darken.bb.common.root.javaroot.internal.RootHost
import eu.darken.bb.common.root.javaroot.internal.RootIPC
import eu.darken.bb.common.shell.RootProcessShell
import eu.darken.bb.common.shell.SharedShell
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import kotlin.system.exitProcess


/**
 * This class' main method will be launched as root. You can access any other class from your
 * package, but not instances - this is a separate process from the UI.
 */
@SuppressLint("UnsafeDynamicallyLoadedCode")
class JavaRootHost constructor(args: List<String>) : SharedHolder.HasKeepAlive<Any>, RootHost() {

    override val keepAlive = SharedHolder.createKeepAlive(TAG)
    private val keepAliveToken: SharedHolder.Resource<*>

    private val pathToAPK: String
    private val component: RootComponent

    @RootProcessShell @Inject lateinit var sharedShell: SharedShell
    @Inject lateinit var connection: Lazy<JavaRootConnectionImpl>
    @Inject lateinit var rootIpcFactory: RootIPC.Factory

    init {
        Log.d(TAG, "init1(args=${args})")

        if (BuildConfig.DEBUG) Logging.install(LogCatLogger())
        log(TAG, INFO) { "init2(args=$args)" }

        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            log(TAG, ERROR) { "Uncaught exception within JavaRootHost: ${throwable.asLog()}" }
            if (oldHandler != null) oldHandler.uncaughtException(thread, throwable)
            else exitProcess(1)
        }

        require(args.isNotEmpty()) { "JavaRootHost: ARGS can't be empty" }

        pathToAPK = args[0]
        log(TAG) { "PKG [${args[0]}]" }

        var usedArgs = 1
        for (i in usedArgs until args.size) {
            val arg = args[i]
            if (arg.startsWith("/") && arg.endsWith(".so")) {
                // assume this is the path to a native library we want to load this has to be done before you can make any JNI calls
                log(TAG) { "Loading [$arg]" }

                Runtime.getRuntime().load(arg)
                usedArgs = i + 1
            }
        }

        // As we made sure to prepend APK and native libs, you can do other parameter parsing here like so
        for (i in usedArgs until args.size) {
            log(TAG) { "Unused ARGS: [${i - usedArgs}] [${args[i]}]" }
        }

        component = DaggerRootComponent.builder().application(systemContext).build().also {
            it.inject(this)
        }

        log(TAG) { "Running on threadId=${Thread.currentThread().id}" }
        //        Debugger.waitFor(true)

        keepAliveToken = keepAlive.get()
        sharedShell.keepAliveWith(this)
    }

    private fun run() {
        log(TAG) { "Starting IPC connection via $rootIpcFactory" }
        val ipc = rootIpcFactory.create(
            packageName = BuildConfig.APPLICATION_ID,
            userProvidedBinder = connection.get(),
        )
        log(TAG) { "IPC created: $ipc" }

        try {
            ipc.broadcast()
        } catch (e: TimeoutException) {
            log(TAG, ERROR) { "Non-root process did not connect in a timely fashion" }
        }

        keepAliveToken.close()
    }


    companion object {
        internal val TAG = logTag("Root", "Java", "Host")

        @JvmStatic fun main(args: Array<String>) {
            val runner = JavaRootHost(args.toList())
            log(TAG) { "run():START" }
            runner.run()
            log(TAG) { "run():FINISHED" }
        }
    }
}
