package eu.darken.bb.main.ui.settings.support

import androidx.annotation.Keep
import eu.darken.bb.GeneralSettings
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import eu.darken.bb.main.ui.settings.debug.SupportFragmentVDC
import javax.inject.Inject

@Keep
class SupportFragment : SmartPreferenceFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: SupportFragmentVDC by vdcs { vdcSource }

    override val preferenceFile: Int = R.xml.preferences_support
    @Inject lateinit var generalSettings: GeneralSettings

    override val settings: GeneralSettings by lazy { generalSettings }

}