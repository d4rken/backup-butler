package eu.darken.bb.common.debug

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import android.util.Log
import com.bugsnag.android.Bugsnag
import com.uber.rxdogtag.RxDogTag
import dagger.Lazy
import eu.darken.bb.App
import eu.darken.bb.Bugs
import eu.darken.bb.BuildConfig
import eu.darken.bb.common.ApiHelper
import eu.darken.bb.common.HotData
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.debug.bugsnag.BugsnagErrorHandler
import eu.darken.bb.common.debug.bugsnag.NOPBugsnagErrorHandler
import eu.darken.bb.common.debug.timber.BugsnagTree
import io.reactivex.Observable
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import java.io.InterruptedIOException
import javax.inject.Inject

@PerApp
class BBDebug @Inject constructor(
        @AppContext private val context: Context,
        moduleFactories: Set<@JvmSuppressWildcards DebugModule.Factory<out DebugModule>>,
        private val installId: InstallId,
        private val errorHandlerSrc: Lazy<BugsnagErrorHandler>,
        private val noopHandlerSrc: Lazy<NOPBugsnagErrorHandler>,
        private val bugsnagTreeSrc: Lazy<BugsnagTree>
) : DebugModuleHost {

    private var preferences: SharedPreferences = context.getSharedPreferences("debug_settings", Context.MODE_PRIVATE)
    private val optionsUpdater = HotData(DebugOptions.default())
    private val modules = mutableSetOf<DebugModule>()

    init {
        RxDogTag.builder().install()

        observeOptions().subscribe { Timber.tag(TAG).d("Updated debug options: $it") }

        if (BuildConfig.DEBUG) {
            val builder = StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()

            @SuppressLint("NewApi")
            if (ApiHelper.hasMarshmallow()) builder.penaltyDeathOnCleartextNetwork()
            @SuppressLint("NewApi")
            if (ApiHelper.hasAndroidNMR1()) builder.penaltyDeathOnFileUriExposure()

            StrictMode.setVmPolicy(builder.build())

            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .permitDiskReads()
                    .permitDiskWrites()
                    .build())

            submit { it.copy(level = Log.VERBOSE) }
        }

        RxJavaPlugins.setErrorHandler { error ->
            if (error is UndeliverableException) {
                when (error.cause) {
                    is InterruptedException, is InterruptedIOException -> {
                        // Swallowed errors
                        Timber.tag(TAG).d(error.cause, "Swallowed interrupt exception")
                    }
                    else -> {
                        Timber.tag(TAG).w(error, "Undesired UndeliverableException")
                        if (BuildConfig.DEBUG) {
                            // On debug builds we want to crash to be aware of this error
                            val currentThread = Thread.currentThread()
                            currentThread.uncaughtExceptionHandler.uncaughtException(currentThread, error)
                        } else {
                            Bugs.track(error)
                        }
                    }
                }
            } else {
                Timber.tag(TAG).e(error, "Unexpected uncaught error")
                val currentThread = Thread.currentThread()
                currentThread.uncaughtExceptionHandler.uncaughtException(currentThread, error)
            }
        }

        setupBugSnag()

        moduleFactories.forEach {
            modules.add(it.create(this))
        }
    }

    private fun setupBugSnag() {
        val bugsnagClient = Bugsnag.init(context)
        bugsnagClient.setUserId(installId.installId.toString())

//        if (ReportingPreferencesFragment.isBugReportingDesired(sdmContext)) {
        Timber.plant(bugsnagTreeSrc.get())
        bugsnagClient.setAutoCaptureSessions(true)
        bugsnagClient.beforeNotify(errorHandlerSrc.get())
        Timber.tag(App.TAG).i("Bugsnag setup done!")
//        } else {
        // TODO
//            bugsnagClient.setAutoCaptureSessions(false)
//            bugsnagClient.beforeNotify(noopHandlerSrc.get())
//            Timber.tag(TAG).i("Installing Bugsnag NOP error handler due to user opt-out!")
//        }

    }

    override fun observeOptions(): Observable<DebugOptions> = optionsUpdater.data

    override fun getSettings(): SharedPreferences = preferences

    @SuppressLint("LogNotTimber")
    override fun submit(update: (DebugOptions) -> DebugOptions) {
        optionsUpdater.update(update)
    }

    fun isDebug(): Boolean = optionsUpdater.snapshot.isDebug()

    fun setRecording(recording: Boolean) {
        submit { it.copy(level = Log.VERBOSE, isRecording = recording) }
    }

    companion object {
        private val TAG = App.logTag("Debug")

        fun isDebug() = BuildConfig.BETA || BuildConfig.DEBUG
    }

}
