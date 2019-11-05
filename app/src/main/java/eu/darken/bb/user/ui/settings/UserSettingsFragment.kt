package eu.darken.bb.user.ui.settings

import androidx.annotation.Keep
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import eu.darken.bb.main.ui.settings.debug.UserSettingsFragmentVDC
import eu.darken.bb.user.core.UserSettings
import javax.inject.Inject

@Keep
class UserSettingsFragment : SmartPreferenceFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: UserSettingsFragmentVDC by vdcs { vdcSource }

    @Inject lateinit var userSettings: UserSettings

    override val settings: UserSettings by lazy { userSettings }
    override val preferenceFile: Int = R.xml.preferences_user

}