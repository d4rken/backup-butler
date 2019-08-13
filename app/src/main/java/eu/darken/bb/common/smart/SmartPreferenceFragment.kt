package eu.darken.bb.common.smart

import android.content.SharedPreferences
import android.os.Bundle
import androidx.annotation.XmlRes
import androidx.preference.PreferenceFragmentCompat
import eu.darken.bb.main.core.Settings

abstract class SmartPreferenceFragment
    : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    abstract val settings: Settings

    @get:XmlRes
    abstract val preferenceFile: Int

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = settings.preferenceDataStore
        settings.preferences.registerOnSharedPreferenceChangeListener(this)
        refreshPreferenceScreen()
    }

    override fun onDestroy() {
        settings.preferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    fun refreshPreferenceScreen() {
        if (preferenceScreen != null) preferenceScreen = null
        addPreferencesFromResource(preferenceFile)
        onPreferencesCreated()
    }

    open fun onPreferencesCreated() {

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {

    }
}