package eu.darken.bb

import android.app.Application
import androidx.work.Configuration
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.logging.LogCatLogger
import eu.darken.bb.common.debug.logging.Logging
import eu.darken.bb.main.core.LanguageEnforcer
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.workers.InjectionWorkerFactory
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
open class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: InjectionWorkerFactory

    @Inject lateinit var uiSettings: UISettings
    @Inject lateinit var bbDebug: Lazy<BBDebug>
    @Inject lateinit var languageEnforcer: LanguageEnforcer

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG || BuildConfig.BETA) {
            Logging.install(LogCatLogger())
        }

        bbDebug.get()

        // Sets theme mode
        uiSettings.theme = uiSettings.theme

        setTheme(R.style.AppTheme_Base)

        Timber.tag(TAG).d("onCreate() done!")
        instance = this

        languageEnforcer.setup()
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(android.util.Log.INFO)
        .build()

    companion object {
        internal val TAG = logTag("App")

        lateinit var instance: App

        fun logTag(vararg tags: String): String {
            val sb = StringBuilder("BB:")
            for (i in tags.indices) {
                sb.append(tags[i])
                if (i < tags.size - 1) sb.append(":")
            }
            return sb.toString()
        }
    }
}
