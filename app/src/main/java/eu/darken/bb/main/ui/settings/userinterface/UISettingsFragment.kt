package eu.darken.bb.main.ui.settings.userinterface

import android.content.SharedPreferences
import androidx.preference.ListPreference
import androidx.preference.Preference
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.main.core.UISettings
import javax.inject.Inject

class UISettingsFragment : SmartPreferenceFragment(), AutoInject {

    @Inject lateinit var uiSettings: UISettings

    override val settings: UISettings by lazy { uiSettings }

    override val preferenceFile: Int = R.xml.preferences_ui

    override fun onPreferencesCreated() {
        val themePref = findPreference<ListPreference>(UISettings.PKEY_THEME)!!
        themePref.entries = UISettings.Theme.values().map { getString(it.label) }.toTypedArray()
        themePref.entryValues = UISettings.Theme.values().map { it.identifier }.toTypedArray()
        themePref.setSummary(settings.theme.label)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            UISettings.PKEY_THEME -> findPreference<Preference>(key)?.setSummary(settings.theme.label)
        }
        super.onSharedPreferenceChanged(sharedPreferences, key)
    }

}