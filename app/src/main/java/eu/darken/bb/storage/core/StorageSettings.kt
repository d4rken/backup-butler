package eu.darken.bb.storage.core

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.settings.core.PreferenceStoreMapper
import eu.darken.bb.settings.core.Settings
import javax.inject.Inject

@PerApp
class StorageSettings @Inject constructor(
        @AppContext private val context: Context
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
        internal val TAG = App.logTag("Storage", "Settings")
        private const val PREF_FILE = "settings_storage"
    }
}