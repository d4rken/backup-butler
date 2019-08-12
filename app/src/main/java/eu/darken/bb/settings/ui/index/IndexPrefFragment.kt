package eu.darken.bb.settings.ui.index

import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.main.core.CoreSettings
import eu.darken.bb.settings.core.Settings
import javax.inject.Inject

class IndexPrefFragment : SmartPreferenceFragment(), AutoInject {

    @Inject lateinit var coreSettings: CoreSettings

    override val settings: Settings by lazy { coreSettings }

    override val preferenceFile: Int = R.xml.preferences_index

}