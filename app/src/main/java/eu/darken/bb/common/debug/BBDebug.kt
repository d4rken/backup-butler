package eu.darken.bb.common.debug

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import android.util.Log
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.App
import eu.darken.bb.Bugs
import eu.darken.bb.BuildConfig
import eu.darken.bb.GeneralSettings
import eu.darken.bb.common.ApiHelper
import eu.darken.bb.common.BuildConfigWrap
import eu.darken.bb.common.debug.bugsnag.BugsnagErrorHandler
import eu.darken.bb.common.debug.bugsnag.BugsnagLogger
import eu.darken.bb.common.debug.bugsnag.NOPBugsnagErrorHandler
import eu.darken.bb.common.debug.logging.Logging
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import rxdogtag2.RxDogTag
import timber.log.Timber
import java.io.InterruptedIOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BBDebug @Inject constructor(
    @ApplicationContext private val context: Context,
    @DebugScope private val debugScope: CoroutineScope,
    moduleFactories: Set<@JvmSuppressWildcards DebugModule.Factory<out DebugModule>>,
    private val generalSettings: GeneralSettings,
    private val installId: InstallId,
    private val errorHandlerSrc: Lazy<BugsnagErrorHandler>,
    private val noopHandlerSrc: Lazy<NOPBugsnagErrorHandler>,
    private val bugsnagTreeSrc: Lazy<BugsnagLogger>,
) : DebugModuleHost {

    // TODO provide better scope? limited dispatcher?
    private var preferences: SharedPreferences = context.getSharedPreferences("debug_settings", Context.MODE_PRIVATE)
    private val optionsUpdater = DynamicStateFlow(TAG, debugScope) { DebugOptions.default() }
    private val modules = mutableSetOf<DebugModule>()

    init {
        RxDogTag.builder().install()

        observeOptions()
            .onEach { Timber.tag(TAG).d("Updated debug options: $it") }
            .launchIn(debugScope)

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
                            currentThread.uncaughtExceptionHandler!!.uncaughtException(currentThread, error)
                        } else {
                            Bugs.track(error)
                        }
                    }
                }
            } else {
                Timber.tag(TAG).e(error, "Unexpected uncaught error")
                val currentThread = Thread.currentThread()
                currentThread.uncaughtExceptionHandler!!.uncaughtException(currentThread, error)
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

    override fun observeOptions(): Flow<DebugOptions> = optionsUpdater.flow

    override fun getSettings(): SharedPreferences = preferences

    override fun submit(update: suspend (DebugOptions) -> DebugOptions) {
        log(TAG) { "submit($update)" }
        optionsUpdater.updateAsync(onUpdate = update)
    }

    fun isDebug(): Boolean = runBlocking { optionsUpdater.value().isDebug() }

    fun setRecording(recording: Boolean) {
        log(TAG) { "setRecording($recording)" }
        submit { it.copy(level = Log.VERBOSE, isRecording = recording) }
    }

    companion object {
        private val TAG = logTag("Debug")

        fun isDebug() = BuildConfigWrap.isVerbosebuild
    }

}
