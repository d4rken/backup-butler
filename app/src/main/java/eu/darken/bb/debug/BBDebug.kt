package eu.darken.bb.debug

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import android.util.Log
import eu.darken.bb.App
import eu.darken.bb.BuildConfig
import eu.darken.bb.common.ApiHelper
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.debug.BugTrack
import eu.thedarken.sdm.tools.debug.DebugModuleHost
import io.reactivex.Observable
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
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
    private val debugOptions = BehaviorSubject.createDefault(DebugOptions.default())
    private val modules = mutableSetOf<DebugModule>()

    init {
        observeOptions().subscribe { Timber.tag(TAG).d("Updated debug options: $it") }

        if (BuildConfig.DEBUG) {
            val builder = StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()

            if (ApiHelper.hasMarshmallow()) builder.penaltyDeathOnCleartextNetwork()
            if (ApiHelper.hasAndroidN_MR1()) builder.penaltyDeathOnFileUriExposure()

            StrictMode.setVmPolicy(builder.build())

            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .permitDiskReads()
                    .permitDiskWrites()
                    .build())

            submit(DebugOptions.default().copy(level = Log.VERBOSE))
        }

        RxJavaPlugins.setErrorHandler { error ->
            if (BuildConfig.DEBUG) {
                val currentThread = Thread.currentThread()
                currentThread.uncaughtExceptionHandler.uncaughtException(currentThread, error)
                return@setErrorHandler
            }

            when (error) {
                is UndeliverableException -> {
                    BugTrack.notify(error)
                }
                else -> {
                    val currentThread = Thread.currentThread()
                    currentThread.uncaughtExceptionHandler.uncaughtException(currentThread, error)
                }
            }
        }

        moduleFactories.forEach {
            modules.add(it.create(this))
        }
    }

    override fun observeOptions(): Observable<DebugOptions> = debugOptions

    override fun getSettings(): SharedPreferences = preferences

    @SuppressLint("LogNotTimber")
    override fun submit(options: DebugOptions) {
        Log.d(TAG, "Submitting options: $options")
        debugOptions.onNext(options)
    }

    fun isDebug(): Boolean = observeOptions().blockingFirst().isDebug()

    fun setRecording(recording: Boolean) {
        observeOptions().firstOrError().subscribe { it -> submit(it.copy(level = Log.VERBOSE, isRecording = recording)) }
    }

}
