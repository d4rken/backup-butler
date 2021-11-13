package eu.darken.bb

import android.app.Application
import androidx.work.Configuration
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.logging.LogCatLogger
import eu.darken.bb.common.debug.logging.Logging
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.main.core.LanguageEnforcer
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.workers.InjectionWorkerFactory
import eu.darken.rxshell.extra.RXSDebug
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
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

        // TODO move code somewhere else
        if (BBDebug.isDebug()) {
            System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)
            DEBUG_PROPERTY_NAME
            RXSDebug.setDebug(true)
            Logging.install(LogCatLogger())
            // TODO remove aber migrating to Logging.kt wrapper
            Timber.plant(Timber.DebugTree())
        }

        bbDebug.get()

        // Sets theme mode
        uiSettings.theme = uiSettings.theme

        setTheme(R.style.AppThemeSplash)

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

    }
}