package eu.darken.bb.main.core

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.R
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.preference.PreferenceStoreMapper
import eu.darken.bb.common.preference.createFlowPreference
import eu.darken.bb.settings.core.Settings
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UISettings @Inject constructor(
    @ApplicationContext private val context: Context
) : Settings() {

    override val preferences: SharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

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
        QUICK(R.string.user_interface_mode_quick_label, "startmode_quick"),
        NORMAL(R.string.user_interface_mode_normal_label, "startmode_normal");

        companion object {
            fun fromString(value: String) = values().first { it.identifier == value }
        }
    }

    var startMode: StartMode
        get() {
            val id = preferences.getString(PKEY_STARTMODE, StartMode.QUICK.identifier)!!
            return StartMode.fromString(id)
        }
        set(value) {
            preferences.edit().putString(PKEY_STARTMODE, value.identifier).apply()
        }

    val showDebugPage = preferences.createFlowPreference(
        PKEY_DEBUGPAGE,
        false
    )

    var language: Locale
        get() {
            val tag = preferences.getString(PKEY_LANGUAGE, Locale.getDefault().toLanguageTag())!!
            return Locale.forLanguageTag(tag)
        }
        set(value) {
            val tag = value.toLanguageTag()
            preferences.edit().putString(PKEY_LANGUAGE, tag).apply()
        }


    override val preferenceDataStore: PreferenceDataStore = object : PreferenceStoreMapper() {
        override fun getBoolean(key: String, defValue: Boolean): Boolean = when (key) {
            PKEY_DEBUGPAGE -> showDebugPage.value
            else -> super.getBoolean(key, defValue)
        }

        override fun putBoolean(key: String, value: Boolean) = when (key) {
            PKEY_DEBUGPAGE -> showDebugPage.update { value }
            else -> super.putBoolean(key, value)
        }

        override fun putString(key: String, value: String?) = when (key) {
            PKEY_THEME -> theme = Theme.fromString(value ?: Theme.SYSTEM.identifier)
            PKEY_STARTMODE -> startMode = StartMode.fromString(value ?: StartMode.QUICK.identifier)
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
        internal val TAG = logTag("UI", "Settings")
        private const val PREF_FILE = "settings_ui"
        const val PKEY_THEME = "ui.theme"
        const val PKEY_STARTMODE = "ui.startmode"
        const val PKEY_DEBUGPAGE = "ui.debugpage.show"
        const val PKEY_LANGUAGE = "ui.language.locale"
    }
}