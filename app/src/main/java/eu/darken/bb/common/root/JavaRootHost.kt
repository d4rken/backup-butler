package eu.darken.bb.common.root

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import eu.darken.bb.App
import eu.darken.bb.BuildConfig
import eu.darken.bb.common.root.librootjava.RootIPC
import eu.darken.bb.common.root.librootjava.RootJava
import timber.log.Timber
import java.util.*
import kotlin.reflect.KClass
import kotlin.system.exitProcess


/**
 * This class' main method will be launched as root. You can access any other class from your
 * package, but not instances - this is a separate process from the UI.
 */
@SuppressLint("UnsafeDynamicallyLoadedCode")
class JavaRootHost constructor(args: List<String>) {
    private val pathToAPK: String
    private val context: Context

    init {
        Log.d(TAG, "init(args=${args})")

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        Timber.tag(TAG).i("init(args=%s)", args)

        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.tag(TAG).e("%s", throwable.javaClass.name)
            if (oldHandler != null) oldHandler.uncaughtException(thread, throwable)
            else exitProcess(1)
        }

        require(args.isNotEmpty()) { "JavaRootHost: ARGS can't be empty" }

        pathToAPK = args[0]
        Timber.tag(TAG).d("APK [%s]", pathToAPK)

        var usedArgs = 1
        for (i in usedArgs until args.size) {
            val arg = args[i]
            if (arg.startsWith("/") && arg.endsWith(".so")) {
                // assume this is the path to a native library we want to load this has to be done before you can make any JNI calls
                Timber.tag(TAG).d("Loading [%s]", arg)

                Runtime.getRuntime().load(arg)
                usedArgs = i + 1
            }
        }

        // Make sure LD_LIBRARY_PATH is sane again, so we don't run into issues running shell commands
        RootJava.restoreOriginalLdLibraryPath()

        // As we made sure to prepend APK and native libs, you can do other parameter parsing here like so
        for (i in usedArgs until args.size) {
            Timber.tag(TAG).d("Unused ARGS: [%d] [%s]", i - usedArgs, args[i])
        }

        // Grab a (limited) context
        context = RootJava.getSystemContext()

        Timber.tag(TAG).d("Running on threadId=%d", Thread.currentThread().id)
        //        Debugger.waitFor(true)
    }

    private fun run() {

        initIPC()

    }

    private fun initIPC() {
        Timber.d("initIPC()")

        val ipc = object : JavaRootIPC.Stub() {
            override fun sayHi(): String {
                Timber.tag(TAG).i("Hi")
                return "Hi"
            }
            /* These calls are executed on a different thread, but not necessarily on the same
               one! Binder uses a thread pool, the calls could come in on any one of those
               threads. Guard variable access and method calls accordingly. */

        }


        try {
            /* Send our IPC binder to the non-root part of the app, wait for a connection, and
               don't return until the app has disconnected.

               It is possible to register multiple interfaces (with different codes) in which
               case this connection-waiting/blocking mechanism is not ideal. You could run them
               each in a separate thread or implement your own handling. But really the easiest
               way is just to return other interfaces through methods of a single main interface,
               and register that one here.
            */
            RootIPC(BuildConfig.APPLICATION_ID, ipc, 0, 30 * 1000, true)
        } catch (e: RootIPC.TimeoutException) {
            /* It doesn't make sense to wait for very long, the broadcast is *not* sticky. RootIPCReceiver
               on the non-root side should connect immediately when it sees the broadcast. If it doesn't,
               it doesn't seem likely it ever will. */
            Timber.tag(TAG).e("Non-root process did not connect in a timely fashion")
        }

    }

    companion object {
        val TAG = App.logTag("Root", "Java", "Host")

        /**
         * Call this from non-root code to generate the script to launch the root code
         *
         * @param context Application or activity context
         * @param params  Parameters to pass
         * @param libs    Native libraries to pass (no extension), for example libmynativecode
         * @return Script
         */
        fun getLaunchScript(
                context: Context,
                clazz: KClass<*> = JavaRootHost::class,
                params: Array<String>? = null,
                libs: Array<String>? = null
        ): List<String> {
            // Add some of our parameters to whatever has been passed in
            // Doing it this way is an example of separating parameters you need every time from
            // parameters that may differ based on what the app is doing.
            // If we didn't do this, we'd use params directly in the getLaunchScript call below
            val paramList = mutableListOf<String>()

            // Path to our APK - this is just an example of parameter passing, there are several ways
            // to get the path of the APK from the code running as root without this.
            paramList.add(context.packageCodePath)

            // Add paths to our native libraries
            libs?.forEach { paramList.add(RootJava.getLibraryPath(context, it)) }

            // Originally passed parameters
            if (params != null) Collections.addAll(paramList, *params)

            // Create actual script
            return RootJava.getLaunchScript(context, clazz.java, null, null, paramList.toTypedArray(), context.packageName + ":javaroothost")
        }


        @JvmStatic fun main(args: Array<String>) {
            val runner = JavaRootHost(args.toList())
            Timber.tag(TAG).d("run():START")
            runner.run()
            Timber.tag(TAG).d("run():FINISHED")
        }
    }

}