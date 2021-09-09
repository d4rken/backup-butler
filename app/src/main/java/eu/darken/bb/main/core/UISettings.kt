package eu.darken.bb.main.core

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.App
import eu.darken.bb.R
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UISettings @Inject constructor(
    @ApplicationContext private val context: Context
) : Settings() {

    enum class Theme(
        @StringRes val label: Int,
        val identifier: String
    ) {
        SYSTEM(R.string.userinterface_theme_system_label, "theme_system"),
        DARK(R.string.userinterface_theme_dark_label, "theme_dark"),
        LIGHT(R.string.userinterface_theme_light_label, "theme_light");

        companion object {
            fun fromString(value: String) = values().first { it.identifier == value }
        }
    }

    var theme: Theme
        get() {
            val id = preferences.getString(PKEY_THEME, Theme.DARK.identifier)!!
            return Theme.fromString(id)
        }
        set(value) {
            preferences.edit().putString(PKEY_THEME, value.identifier).apply()
            when (theme) {
                Theme.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                Theme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

    enum class StartMode(
        @StringRes val label: Int,
        val identifier: String
    ) {
        SIMPLE(R.string.startmode_simple_label, "startmode_simple"),
        ADVANCED(R.string.startmode_advanced_label, "startmode_advanced");

        companion object {
            fun fromString(value: String) = values().first { it.identifier == value }
        }
    }

    var startMode: StartMode
        get() {
            val id = preferences.getString(PKEY_STARTMODE, StartMode.SIMPLE.identifier)!!
            return StartMode.fromString(id)
        }
        set(value) {
            preferences.edit().putString(PKEY_STARTMODE, value.identifier).apply()
        }

    var showDebugPage: Boolean
        get() = preferences.getBoolean(PKEY_DEBUGPAGE, false)
        set(value) = preferences.edit().putBoolean(PKEY_DEBUGPAGE, value).apply()

    var showOnboarding: Boolean
        get() = preferences.getBoolean(PKEY_ONBOARDING, true)
        set(value) = preferences.edit().putBoolean(PKEY_ONBOARDING, value).apply()


    var language: Locale
        get() {
            val tag = preferences.getString(PKEY_LANGUAGE, Locale.getDefault().toLanguageTag())!!
            return Locale.forLanguageTag(tag)
        }
        set(value) {
            val tag = value.toLanguageTag()
            preferences.edit().putString(PKEY_LANGUAGE, tag).apply()
        }

    override val preferences: SharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    override val preferenceDataStore: PreferenceDataStore = object : PreferenceStoreMapper() {
        override fun getBoolean(key: String, defValue: Boolean): Boolean = when (key) {
            PKEY_DEBUGPAGE -> showDebugPage
            PKEY_ONBOARDING -> showOnboarding
            else -> super.getBoolean(key, defValue)
        }

        override fun putBoolean(key: String, value: Boolean) = when (key) {
            PKEY_DEBUGPAGE -> showDebugPage = value
            PKEY_ONBOARDING -> showOnboarding = value
            else -> super.putBoolean(key, value)
        }

        override fun putString(key: String, value: String?) = when (key) {
            PKEY_THEME -> theme = Theme.fromString(value ?: Theme.SYSTEM.identifier)
            PKEY_STARTMODE -> startMode = StartMode.fromString(value ?: StartMode.SIMPLE.identifier)
            PKEY_LANGUAGE -> language = value?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
            else -> super.putString(key, value)
        }

        override fun getString(key: String, defValue: String?) = when (key) {
            PKEY_THEME -> theme.identifier
            PKEY_STARTMODE -> startMode.identifier
            PKEY_LANGUAGE -> language.toLanguageTag()
            else -> super.getString(key, defValue)
        }
    }


    companion object {
        internal val TAG = App.logTag("UI", "Settings")
        private const val PREF_FILE = "settings_ui"
        const val PKEY_THEME = "ui.theme"
        const val PKEY_STARTMODE = "ui.startmode"
        const val PKEY_DEBUGPAGE = "ui.debugpage.show"
        const val PKEY_ONBOARDING = "ui.onboarding.show"
        const val PKEY_LANGUAGE = "ui.language.locale"
    }
}