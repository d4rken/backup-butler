package eu.darken.bb.storage.ui.settings

import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.storage.core.StorageSettings
import javax.inject.Inject

class StorageSettingsFragment : SmartPreferenceFragment(), AutoInject {

    @Inject lateinit var uiSettings: StorageSettings

    override val settings: StorageSettings by lazy { uiSettings }

    override val preferenceFile: Int = R.xml.preferences_storage

    override fun onPreferencesCreated() {

    }

}