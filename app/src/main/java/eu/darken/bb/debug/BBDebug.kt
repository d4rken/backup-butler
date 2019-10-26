package eu.darken.bb.debug

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import android.util.Log
import eu.darken.bb.App
import eu.darken.bb.Bugs
import eu.darken.bb.BuildConfig
import eu.darken.bb.common.ApiHelper
import eu.darken.bb.common.HotData
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import io.reactivex.Observable
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import java.io.InterruptedIOException
import javax.inject.Inject

@PerApp
class BBDebug @Inject constructor(
        @AppContext private val context: Context,
        moduleFactories: Set<@JvmSuppressWildcards DebugModule.Factory<out DebugModule>>
) : DebugModuleHost {

    companion object {
        private val TAG = App.logTag("Debug")
        private const val PREF_FILE = "debug_settings"
    }

    private var preferences: SharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
    private val optionsUpdater = HotData(DebugOptions.default())
    private val modules = mutableSetOf<DebugModule>()

    init {
        observeOptions().subscribe { Timber.tag(TAG).d("Updated debug options: $it") }

        if (BuildConfig.DEBUG) {
            val builder = StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()

            if (ApiHelper.hasMarshmallow()) builder.penaltyDeathOnCleartextNetwork()
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

        moduleFactories.forEach {
            modules.add(it.create(this))
        }
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

}
