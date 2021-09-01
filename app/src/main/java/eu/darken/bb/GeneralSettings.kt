package eu.darken.bb

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.main.core.PreferenceStoreMapper
import eu.darken.bb.main.core.Settings
import javax.inject.Inject

@PerApp
class GeneralSettings @Inject constructor(
    @AppContext private val context: Context
) : Settings() {

    var isRootDisabled: Boolean
        get() = preferences.getBoolean(PK_ROOT_DISABLED, false)
        set(value) = preferences.edit().putBoolean(PK_ROOT_DISABLED, value).apply()


    var isBugTrackingEnabled: Boolean
        get() = preferences.getBoolean(PK_BUGTRACKING_ENABLED, true)
        set(value) = preferences.edit().putBoolean(PK_BUGTRACKING_ENABLED, value).apply()

    var isPreviewEnabled: Boolean = true

    override val preferenceDataStore: PreferenceDataStore = object : PreferenceStoreMapper() {
        override fun getBoolean(key: String, defValue: Boolean): Boolean = when (key) {
            PK_ROOT_DISABLED -> isRootDisabled
            PK_BUGTRACKING_ENABLED -> isBugTrackingEnabled
            else -> super.getBoolean(key, defValue)
        }

        override fun putBoolean(key: String, value: Boolean) = when (key) {
            PK_ROOT_DISABLED -> isRootDisabled = value
            PK_BUGTRACKING_ENABLED -> isBugTrackingEnabled = value
            else -> super.putBoolean(key, value)
        }
    }

    override val preferences: SharedPreferences = context.getSharedPreferences("settings_core", Context.MODE_PRIVATE)

    companion object {
        internal val TAG = App.logTag("Core", "Settings")
        private const val PK_ROOT_DISABLED = "core.root.disabled"
        private const val PK_BUGTRACKING_ENABLED = "core.bugtracking.enabled"
        const val PKEY_RECORD_DEBUG = "core.debug.recordlog"
    }
}