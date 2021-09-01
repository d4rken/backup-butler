package eu.darken.bb.task.core

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore
import eu.darken.bb.App
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.main.core.PreferenceStoreMapper
import eu.darken.bb.main.core.Settings
import javax.inject.Inject

@PerApp
class TaskSettings @Inject constructor(
    @AppContext private val context: Context
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
        internal val TAG = App.logTag("Backup", "Settings")
    }
}