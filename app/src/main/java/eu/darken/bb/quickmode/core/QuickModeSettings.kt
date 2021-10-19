package eu.darken.bb.quickmode.core

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.preference.PreferenceStoreMapper
import eu.darken.bb.common.preference.createObservablePreference
import eu.darken.bb.settings.core.Settings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuickModeSettings @Inject constructor(
    @ApplicationContext private val context: Context
) : Settings() {

    override val preferences: SharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    val isHintAdvancedModeDismissed = preferences.createObservablePreference(PK_HINT_ADVANCED_MODE_DISMISSED, false)

    var rawConfigApps: String?
        get() = preferences.getString(PK_CONFIG_APPS, null)
        set(value) = preferences.edit().putString(PK_CONFIG_APPS, value).apply()

    var rawConfigFiles: String?
        get() = preferences.getString(PK_CONFIG_FILES, null)
        set(value) = preferences.edit().putString(PK_CONFIG_FILES, value).apply()


    override val preferenceDataStore: PreferenceDataStore = object : PreferenceStoreMapper() {
        override fun getString(key: String, defValue: String?): String? = when (key) {
            PK_CONFIG_APPS -> rawConfigApps
            else -> super.getString(key, defValue)
        }

        override fun getBoolean(key: String, defValue: Boolean): Boolean = when (key) {
            PK_HINT_ADVANCED_MODE_DISMISSED -> isHintAdvancedModeDismissed.value
            else -> super.getBoolean(key, defValue)
        }

        override fun putBoolean(key: String, value: Boolean) = when (key) {
            PK_HINT_ADVANCED_MODE_DISMISSED -> isHintAdvancedModeDismissed.update { value }
            else -> super.putBoolean(key, value)
        }
    }


    companion object {
        internal val TAG = logTag("QuickMode", "Settings")
        const val PREF_FILE = "settings_quickmode"

        private const val PK_CONFIG_APPS = "quickmode.config.apps.raw"
        private const val PK_CONFIG_FILES = "quickmode.config.files.raw"
        private const val PK_HINT_ADVANCED_MODE_DISMISSED = "quickmode.hint.advancedmode.dismissed"
    }
}