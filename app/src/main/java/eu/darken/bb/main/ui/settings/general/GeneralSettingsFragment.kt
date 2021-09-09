package eu.darken.bb.main.ui.settings.general

import androidx.annotation.Keep
import androidx.lifecycle.Observer
import androidx.preference.Preference
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.GeneralSettings
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import javax.inject.Inject

@Keep
@AndroidEntryPoint
class GeneralSettingsFragment : SmartPreferenceFragment() {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: GeneralSettingsFragmentVDC by vdcs { vdcSource }

    @Inject lateinit var debugSettings: GeneralSettings

    override val settings: GeneralSettings by lazy { debugSettings }
    override val preferenceFile: Int = R.xml.preferences_general

    private val recordPref by lazy { findPreference<Preference>(GeneralSettings.PKEY_RECORD_DEBUG)!! }

    override fun onPreferencesCreated() {

        recordPref.setOnPreferenceClickListener {
            vdc.startDebugLog()
            true
        }

        vdc.state.observe(this, Observer { state ->
            recordPref.summary = when {
                !state.isRecording -> getString(R.string.debug_record_log_desc)
                else -> state.recordingPath
            }
            recordPref.isEnabled = !state.isRecording
        })

        super.onPreferencesCreated()
    }

}