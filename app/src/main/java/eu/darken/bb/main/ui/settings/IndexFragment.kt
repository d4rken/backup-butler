package eu.darken.bb.main.ui.settings

import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.main.core.CoreSettings
import eu.darken.bb.main.core.Settings
import javax.inject.Inject

class IndexFragment : SmartPreferenceFragment(), AutoInject {

    @Inject lateinit var coreSettings: CoreSettings

    override val settings: Settings by lazy { coreSettings }

    override val preferenceFile: Int = R.xml.preferences_index

}