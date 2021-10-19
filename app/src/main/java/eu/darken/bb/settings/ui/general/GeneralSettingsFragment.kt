package eu.darken.bb.settings.ui.general

import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.GeneralSettings
import eu.darken.bb.R
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartPreferenceFragment
import javax.inject.Inject

@Keep
@AndroidEntryPoint
class GeneralSettingsFragment : SmartPreferenceFragment() {

    private val vdc: GeneralSettingsFragmentVDC by viewModels()

    @Inject lateinit var debugSettings: GeneralSettings

    override val settings: GeneralSettings by lazy { debugSettings }
    override val preferenceFile: Int = R.xml.preferences_general

    private val recordPref by lazy { findPreference<Preference>(GeneralSettings.PKEY_RECORD_DEBUG)!! }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recordPref.setOnPreferenceClickListener {
            vdc.startDebugLog()
            true
        }

        vdc.state.observe2(this) { state ->
            recordPref.summary = when {
                !state.isRecording -> getString(R.string.debug_record_log_desc)
                else -> state.recordingPath
            }
            recordPref.isEnabled = !state.isRecording
        }
    }

}