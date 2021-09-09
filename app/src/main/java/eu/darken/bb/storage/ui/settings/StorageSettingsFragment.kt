package eu.darken.bb.storage.ui.settings

import androidx.annotation.Keep
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.storage.core.StorageSettings
import javax.inject.Inject

@Keep
@AndroidEntryPoint
class StorageSettingsFragment : SmartPreferenceFragment() {

    @Inject lateinit var uiSettings: StorageSettings

    override val settings: StorageSettings by lazy { uiSettings }

    override val preferenceFile: Int = R.xml.preferences_storage

    override fun onPreferencesCreated() {

    }

}