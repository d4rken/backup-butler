package eu.darken.bb.main.ui.settings.acks

import androidx.annotation.Keep
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.GeneralSettings
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import eu.darken.bb.main.ui.settings.general.GeneralSettingsFragmentVDC
import javax.inject.Inject

@Keep
@AndroidEntryPoint
class AcknowledgementsFragment : SmartPreferenceFragment() {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: GeneralSettingsFragmentVDC by vdcs { vdcSource }

    override val preferenceFile: Int = R.xml.preferences_acknowledgements
    @Inject lateinit var debugSettings: GeneralSettings

    override val settings: GeneralSettings by lazy { debugSettings }

}