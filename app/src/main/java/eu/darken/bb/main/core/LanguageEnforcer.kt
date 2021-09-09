package eu.darken.bb.main.core

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import eu.darken.bb.App
import eu.darken.bb.R
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LanguageEnforcer @Inject constructor(
    private val application: Application,
    private val uiSettings: UISettings
) {
    private val defaultListOfTranslators: String by lazy {
        val configuration = Configuration(application.resources.configuration)
        configuration.setLocale(Locale.ENGLISH)
        val localizedResources = application.createConfigurationContext(configuration).resources
        localizedResources.getString(R.string.language_list_of_translators)
    }
    val supportedLocales: List<Locale>
        get() {
            return application.resources.assets.locales.map { Locale.forLanguageTag(it) }
        }

    var currentLocale: Locale
        get() {
            return uiSettings.language
        }
        set(value) {
            Timber.tag(TAG).i("Setting enforced language to %s", value)
            uiSettings.language = value
            enforceLanguage(application.resources)
        }

    fun lookup(locale: Locale): Language {
        val configuration = Configuration(application.resources.configuration)
        configuration.setLocale(locale)
        val localizedResources = application.createConfigurationContext(configuration).resources
        val translatorsString = localizedResources.getString(R.string.language_list_of_translators)
        val translators = translatorsString.split(";")
        return Language(
            locale = locale,
            translators = translators,
            isTranslated = defaultListOfTranslators != translatorsString || locale.language == "en"
        )
    }

    private val activityCallback = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) =
            enforceLanguage(activity.resources)

        override fun onActivityStarted(activity: Activity) = Unit

        override fun onActivityResumed(activity: Activity) = Unit

        override fun onActivityPaused(activity: Activity) = Unit

        override fun onActivityStopped(activity: Activity) = Unit

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

        override fun onActivityDestroyed(activity: Activity) = Unit
    }

    fun setup() {
        enforceLanguage()
        application.registerActivityLifecycleCallbacks(activityCallback)
    }

    fun tearDown() {
        application.unregisterActivityLifecycleCallbacks(activityCallback)
    }

    @Suppress("DEPRECATION")
    internal fun enforceLanguage(resources: Resources = application.resources) {
        val targetLocale = currentLocale

        Timber.tag(TAG).i("Enforcing language: %s", targetLocale)

        try {
            if (Locale.getDefault() != targetLocale) {
                Locale.setDefault(targetLocale)
            }
        } catch (e: SecurityException) {
            Timber.tag(TAG).e(e, "Failed to enforce default locale.")
        }

        val config = resources.configuration
        if (config.locale != targetLocale) {
            config.locale = targetLocale
            resources.updateConfiguration(config, null)
        }
    }

    fun updateLanguage(language: Language, resources: Resources?) {
        currentLocale = language.locale
        enforceLanguage(resources ?: application.resources)
    }

    data class Language(
        val locale: Locale,
        val translators: List<String>,
        val isTranslated: Boolean
    ) {
        val localeFormatted: String = locale.formatLocale()

        val translatorsFormatted: String
            get() {
                return translators.joinToString()
            }
    }

    companion object {
        internal val TAG = App.logTag("UI", "LanguageEnforcer")

    }
}

internal fun Locale.formatLocale(): String {
    val locale = this
    val line = StringBuilder(locale.displayLanguage)
    if (locale.displayCountry.isNotEmpty()) line.append(" (").append(locale.displayCountry).append(")")
    line.append(" [").append(locale.toString()).append("]")
    return line.toString()
}