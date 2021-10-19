package eu.darken.bb.schedule.core

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.preference.PreferenceStoreMapper
import eu.darken.bb.settings.core.Settings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SchedulerSettings @Inject constructor(
    @ApplicationContext private val context: Context
) : Settings() {

    override val preferenceDataStore: PreferenceDataStore = object : PreferenceStoreMapper() {
        override fun getBoolean(key: String, defValue: Boolean): Boolean = when (key) {

            else -> super.getBoolean(key, defValue)
        }

        override fun putBoolean(key: String, value: Boolean) = when (key) {

            else -> super.putBoolean(key, value)
        }
    }

    override val preferences: SharedPreferences = context.getSharedPreferences("settings_core", Context.MODE_PRIVATE)

    companion object {
        internal val TAG = logTag("Backup", "Settings")
    }
}