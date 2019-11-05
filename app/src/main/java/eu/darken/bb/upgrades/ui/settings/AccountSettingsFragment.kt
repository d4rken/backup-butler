package eu.darken.bb.upgrades.ui.settings

import androidx.annotation.Keep
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import eu.darken.bb.main.core.DebugSettings
import eu.darken.bb.main.ui.settings.debug.AccountSettingsFragmentVDC
import javax.inject.Inject

@Keep
class AccountSettingsFragment : SmartPreferenceFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: AccountSettingsFragmentVDC by vdcs { vdcSource }

    @Inject lateinit var debugSettings: DebugSettings

    override val settings: DebugSettings by lazy { debugSettings }
    override val preferenceFile: Int = R.xml.preferences_account

}