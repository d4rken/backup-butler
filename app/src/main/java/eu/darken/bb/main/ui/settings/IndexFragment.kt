package eu.darken.bb.main.ui.settings

import eu.darken.bb.GeneralSettings
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.main.core.Settings
import javax.inject.Inject

class IndexFragment : SmartPreferenceFragment(), AutoInject {

    @Inject lateinit var generalSettings: GeneralSettings

    override val settings: Settings by lazy { generalSettings }

    override val preferenceFile: Int = R.xml.preferences_index

}