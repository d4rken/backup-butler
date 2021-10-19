package eu.darken.bb.storage.core

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
class StorageSettings @Inject constructor(
    @ApplicationContext private val context: Context
) : Settings() {

    override val preferences: SharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    override val preferenceDataStore: PreferenceDataStore = object : PreferenceStoreMapper() {
        override fun getBoolean(key: String, defValue: Boolean): Boolean = when (key) {
            else -> super.getBoolean(key, defValue)
        }

        override fun putBoolean(key: String, value: Boolean) = when (key) {

            else -> super.putBoolean(key, value)
        }

        override fun putString(key: String, value: String?) = when (key) {
            else -> super.putString(key, value)
        }

        override fun getString(key: String, defValue: String?) = when (key) {
            else -> super.getString(key, defValue)
        }
    }


    companion object {
        internal val TAG = logTag("Storage", "Settings")
        private const val PREF_FILE = "settings_storage"
    }
}