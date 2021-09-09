package eu.darken.bb.common.root.core.javaroot

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import dagger.Lazy
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eu.darken.bb.App
import eu.darken.bb.BuildConfig
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.root.librootjava.RootIPC
import eu.darken.bb.common.root.librootjava.RootJava
import eu.darken.bb.common.shell.SharedShell
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.system.exitProcess


/**
 * This class' main method will be launched as root. You can access any other class from your
 * package, but not instances - this is a separate process from the UI.
 */
@SuppressLint("UnsafeDynamicallyLoadedCode")
class JavaRootHost constructor(args: List<String>) : SharedHolder.HasKeepAlive<Any> {

    override val keepAlive = SharedHolder.createKeepAlive(TAG)
    private val keepAliveToken: SharedHolder.Resource<*>

    private val pathToAPK: String
    private val component: RootComponent

    @Inject lateinit var sharedShell: SharedShell
    @Inject lateinit var connection: Lazy<JavaRootConnectionImpl>

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
        component = DaggerRootComponent.builder()
            .application(RootJava.getSystemContext())
            .build()
        component.inject(this)


        Timber.tag(TAG).d("Running on threadId=%d", Thread.currentThread().id)
        //        Debugger.waitFor(true)

        keepAliveToken = keepAlive.get()
        sharedShell.keepAliveWith(this)
    }

    private fun run() {
        Timber.tag(TAG).d("Starting IPC connection")

        try {
            RootIPC(BuildConfig.APPLICATION_ID, connection.get(), 0, 30 * 1000, true)
        } catch (e: RootIPC.TimeoutException) {
            Timber.tag(TAG).e("Non-root process did not connect in a timely fashion")
        }

        keepAliveToken.close()
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
            libs: Array<String>? = null,
            processName: String? = context.packageName + ":javaroothost"
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
            return RootJava.getLaunchScript(context, clazz.java, null, null, paramList.toTypedArray(), processName)
        }


        @JvmStatic fun main(args: Array<String>) {
            val runner = JavaRootHost(args.toList())
            Timber.tag(TAG).d("run():START")
            runner.run()
            Timber.tag(TAG).d("run():FINISHED")
        }
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface JavaRootHostEntryPoint {
        fun inject(host: JavaRootHost)
    }
}
