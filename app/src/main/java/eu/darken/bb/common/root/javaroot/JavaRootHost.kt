package eu.darken.bb.common.root.javaroot

import android.annotation.SuppressLint
import android.util.Log
import dagger.Lazy
import eu.darken.bb.BuildConfig
import eu.darken.bb.common.BuildConfigWrap
import eu.darken.bb.common.debug.logging.*
import eu.darken.bb.common.debug.logging.Logging.Priority.*
import eu.darken.bb.common.root.javaroot.internal.RootHost
import eu.darken.bb.common.root.javaroot.internal.RootIPC
import eu.darken.bb.common.sharedresource.HasSharedResource
import eu.darken.bb.common.sharedresource.Resource
import eu.darken.bb.common.sharedresource.SharedResource
import eu.darken.bb.common.shell.RootProcessShell
import eu.darken.bb.common.shell.SharedShell
import eu.darken.rxshell.extra.RXSDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import kotlin.system.exitProcess


/**
 * This class' main method will be launched as root. You can access any other class from your
 * package, but not instances - this is a separate process from the UI.
 */
@SuppressLint("UnsafeDynamicallyLoadedCode")
class JavaRootHost constructor(_args: List<String>) : HasSharedResource<Any>, RootHost() {

    override val sharedResource = SharedResource.createKeepAlive(
        TAG,
        GlobalScope + Dispatchers.IO
    )

    private val ourPkgName: String
    private val isDebug: Boolean
    private val component: RootComponent

    @RootProcessShell @Inject lateinit var sharedShell: SharedShell
    @Inject lateinit var connection: Lazy<JavaRootConnectionImpl>
    @Inject lateinit var rootIpcFactory: RootIPC.Factory

    init {
        Log.d(TAG, "init1(args=${_args})")
        val args = _args.toMutableList()
        isDebug = args.remove(DEBUG_FLAG)

        if (BuildConfigWrap.isVerbosebuild || isDebug) {
            Logging.install(LogCatLogger())
            Timber.plant(Timber.DebugTree())
            RXSDebug.setDebug(true)
            log(TAG) { "isVerbosebuild=${BuildConfigWrap.isVerbosebuild}, isDebug=$isDebug" }
        }
        log(TAG, INFO) { "init2(args=$args)" }

        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            log(TAG, ERROR) { "Uncaught exception within JavaRootHost: ${throwable.asLog()}" }
            if (oldHandler != null) oldHandler.uncaughtException(thread, throwable)
            else exitProcess(1)
        }

        require(args.isNotEmpty()) { "JavaRootHost: ARGS can't be empty" }

        ourPkgName = args[0]
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
    }

    private suspend fun run() {
        log(TAG) { "Starting IPC connection via $rootIpcFactory" }
        val ipc = rootIpcFactory.create(
            packageName = BuildConfig.APPLICATION_ID,
            userProvidedBinder = connection.get(),
        )
        log(TAG) { "IPC created: $ipc" }

        val keepAliveToken: Resource<*> = sharedResource.get()

        log(TAG) { "Launching SharedShell with root" }
        sharedShell.addParent(this.sharedResource)

        try {
            log(TAG) { "Ready, now broadcasting..." }
            ipc.broadcastAndWait()
        } catch (e: TimeoutException) {
            log(TAG, ERROR) { "Non-root process did not connect in a timely fashion" }
        }

        keepAliveToken.close()
    }


    companion object {
        const val DEBUG_FLAG = "BB-DEBUG"
        internal val TAG = logTag("Root", "Java", "Host")

        @JvmStatic fun main(args: Array<String>) {
            log(TAG) { "main(args=$args)" }
            val runner = JavaRootHost(args.toList())

            log(TAG, VERBOSE) { "run():START" }
            runBlocking {
                runner.run()
            }
            log(TAG, VERBOSE) { "run():FINISHED" }
        }
    }
}
