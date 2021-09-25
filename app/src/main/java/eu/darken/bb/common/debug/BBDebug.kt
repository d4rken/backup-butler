package eu.darken.bb.common.debug

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import android.util.Log
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import com.uber.rxdogtag.RxDogTag
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.App
import eu.darken.bb.Bugs
import eu.darken.bb.BuildConfig
import eu.darken.bb.GeneralSettings
import eu.darken.bb.common.ApiHelper
import eu.darken.bb.common.HotData
import eu.darken.bb.common.debug.bugsnag.BugsnagErrorHandler
import eu.darken.bb.common.debug.bugsnag.BugsnagLogger
import eu.darken.bb.common.debug.bugsnag.NOPBugsnagErrorHandler
import eu.darken.bb.common.debug.logging.Logging
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.exceptions.UndeliverableException
import timber.log.Timber
import java.io.InterruptedIOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BBDebug @Inject constructor(
    @ApplicationContext private val context: Context,
    moduleFactories: Set<@JvmSuppressWildcards DebugModule.Factory<out DebugModule>>,
    private val generalSettings: GeneralSettings,
    private val installId: InstallId,
    private val errorHandlerSrc: Lazy<BugsnagErrorHandler>,
    private val noopHandlerSrc: Lazy<NOPBugsnagErrorHandler>,
    private val bugsnagTreeSrc: Lazy<BugsnagLogger>
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

            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .permitDiskReads()
                    .permitDiskWrites()
                    .build()
            )

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
        val config = Configuration.load(context).apply {
            setUser(installId.installId.toString(), null, null)
            autoTrackSessions = generalSettings.isBugTrackingEnabled

            if (generalSettings.isBugTrackingEnabled) {
                Logging.install(bugsnagTreeSrc.get())
                addOnError(errorHandlerSrc.get())
                Timber.tag(App.TAG).i("Bugsnag setup done!")
            } else {
                addOnError(noopHandlerSrc.get())
                Timber.tag(TAG).i("Installing Bugsnag NOP error handler due to user opt-out!")
            }
        }
        Bugsnag.start(context, config)
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
