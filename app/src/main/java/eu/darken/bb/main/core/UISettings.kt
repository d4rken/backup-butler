package eu.darken.bb.main.core

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceDataStore
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import javax.inject.Inject

@PerApp
class UISettings @Inject constructor(
        @AppContext private val context: Context
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

    enum class StartMode(
            @StringRes val label: Int,
            val identifier: String
    ) {
        ONBOARDING
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

    var showDebugPage: Boolean
        get() = preferences.getBoolean(PKEY_DEBUGPAGE, false)
        set(value) = preferences.edit().putBoolean(PKEY_DEBUGPAGE, value).apply()

    override val preferences: SharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    override val preferenceDataStore: PreferenceDataStore = object : PreferenceStoreMapper() {
        override fun getBoolean(key: String, defValue: Boolean): Boolean = when (key) {
            PKEY_DEBUGPAGE -> showDebugPage
            else -> super.getBoolean(key, defValue)
        }

        override fun putBoolean(key: String, value: Boolean) = when (key) {
            PKEY_DEBUGPAGE -> showDebugPage = value
            else -> super.putBoolean(key, value)
        }

        override fun putString(key: String, value: String?) = when (key) {
            PKEY_THEME -> theme = Theme.fromString(value ?: Theme.SYSTEM.identifier)
            else -> super.putString(key, value)
        }

        override fun getString(key: String, defValue: String?) = when (key) {
            PKEY_THEME -> theme.identifier
            else -> super.getString(key, defValue)
        }
    }


    companion object {
        internal val TAG = App.logTag("UI", "Settings")
        private const val PREF_FILE = "settings_ui"
        const val PKEY_THEME = "ui.theme"
        const val PKEY_DEBUGPAGE = "ui.debugpage.show"
    }
}