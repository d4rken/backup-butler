package eu.darken.bb.settings.ui.ui

import android.content.SharedPreferences
import androidx.annotation.Keep
import androidx.preference.ListPreference
import androidx.preference.Preference
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.main.core.LanguageEnforcer
import eu.darken.bb.main.core.UISettings
import javax.inject.Inject

@Keep
@AndroidEntryPoint
class UISettingsFragment : SmartPreferenceFragment() {

    @Inject lateinit var uiSettings: UISettings

    override val settings: UISettings by lazy { uiSettings }

    override val preferenceFile: Int = R.xml.preferences_ui

    @Inject lateinit var languageEnforcer: LanguageEnforcer

    val themePref by lazy { findPreference<ListPreference>(UISettings.PKEY_THEME)!! }
    val startModePref by lazy { findPreference<ListPreference>(UISettings.PKEY_STARTMODE)!! }
    val pickLanguage by lazy { findPreference<Preference>("ui.language.picker")!! }

    override fun onPreferencesCreated() {
        themePref.apply {
            entries = UISettings.Theme.values().map { getString(it.label) }.toTypedArray()
            entryValues = UISettings.Theme.values().map { it.identifier }.toTypedArray()
            setSummary(settings.theme.label)
        }
        startModePref.apply {
            entries = UISettings.StartMode.values().map { getString(it.label) }.toTypedArray()
            entryValues = UISettings.StartMode.values().map { it.identifier }.toTypedArray()
            setSummary(settings.startMode.label)
        }
    }

    override fun onResume() {
        pickLanguage.summary = languageEnforcer.lookup(languageEnforcer.currentLocale).localeFormatted
        super.onResume()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            UISettings.PKEY_THEME -> themePref.setSummary(settings.theme.label)
            UISettings.PKEY_STARTMODE -> startModePref.setSummary(settings.startMode.label)
        }
        super.onSharedPreferenceChanged(sharedPreferences, key)
    }

}