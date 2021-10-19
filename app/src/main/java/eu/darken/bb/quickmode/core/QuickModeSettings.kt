package eu.darken.bb.quickmode.core

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.preference.createObservablePreference
import eu.darken.bb.main.core.PreferenceStoreMapper
import eu.darken.bb.main.core.Settings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuickModeSettings @Inject constructor(
    @ApplicationContext private val context: Context
) : Settings() {

    override val preferences: SharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    val isHintAdvancedModeDismissed = preferences.createObservablePreference(PK_HINT_ADVANCED_MODE_DISMISSED, false)

    override val preferenceDataStore: PreferenceDataStore = object : PreferenceStoreMapper() {
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
        const val PREF_FILE = "settings_mode_simple"

        private const val PK_HINT_ADVANCED_MODE_DISMISSED = "mode.simple.hint.advancedmode.dismissed"
    }
}