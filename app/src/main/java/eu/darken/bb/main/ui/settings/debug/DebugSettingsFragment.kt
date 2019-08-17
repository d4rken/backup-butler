package eu.darken.bb.main.ui.settings.debug

import androidx.lifecycle.Observer
import androidx.preference.Preference
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import eu.darken.bb.main.core.DebugSettings
import javax.inject.Inject

class DebugSettingsFragment : SmartPreferenceFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: DebugSettingsFragmentVDC by vdcs { vdcSource }

    @Inject lateinit var debugSettings: DebugSettings

    override val settings: DebugSettings by lazy { debugSettings }
    override val preferenceFile: Int = R.xml.preferences_debug

    private val recordPref by lazy { findPreference<Preference>(DebugSettings.PKEY_RECORD)!! }

    override fun onPreferencesCreated() {

        recordPref.setOnPreferenceClickListener {
            vdc.startDebugLog()
            true
        }

        vdc.state.observe(this, Observer { state ->
            recordPref.summary = when {
                !state.isRecording -> getString(R.string.record_debuglog_desc)
                else -> state.recordingPath
            }
            recordPref.isEnabled = !state.isRecording
        })

        super.onPreferencesCreated()
    }

}