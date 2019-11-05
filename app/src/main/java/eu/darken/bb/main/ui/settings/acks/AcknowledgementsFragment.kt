package eu.darken.bb.main.ui.settings.acks

import androidx.annotation.Keep
import eu.darken.bb.GeneralSettings
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import eu.darken.bb.main.ui.settings.debug.GeneralSettingsFragmentVDC
import javax.inject.Inject

@Keep
class AcknowledgementsFragment : SmartPreferenceFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: GeneralSettingsFragmentVDC by vdcs { vdcSource }

    override val preferenceFile: Int = R.xml.preferences_acknowledgements
    @Inject lateinit var debugSettings: GeneralSettings

    override val settings: GeneralSettings by lazy { debugSettings }

}