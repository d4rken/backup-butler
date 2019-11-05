package eu.darken.bb.main.ui.settings.general

import androidx.annotation.Keep
import androidx.lifecycle.Observer
import androidx.preference.Preference
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import eu.darken.bb.main.core.DebugSettings
import eu.darken.bb.main.ui.settings.debug.GeneralSettingsFragmentVDC
import javax.inject.Inject

@Keep
class GeneralSettingsFragment : SmartPreferenceFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: GeneralSettingsFragmentVDC by vdcs { vdcSource }

    @Inject lateinit var debugSettings: DebugSettings

    override val settings: DebugSettings by lazy { debugSettings }
    override val preferenceFile: Int = R.xml.preferences_general

    private val recordPref by lazy { findPreference<Preference>(DebugSettings.PKEY_RECORD)!! }

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