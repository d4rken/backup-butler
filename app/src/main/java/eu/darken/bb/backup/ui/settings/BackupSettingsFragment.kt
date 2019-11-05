package eu.darken.bb.backup.ui.settings

import androidx.annotation.Keep
import eu.darken.bb.R
import eu.darken.bb.backup.core.BackupSettings
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartPreferenceFragment
import javax.inject.Inject

@Keep
class BackupSettingsFragment : SmartPreferenceFragment(), AutoInject {

    @Inject lateinit var uiSettings: BackupSettings

    override val settings: BackupSettings by lazy { uiSettings }

    override val preferenceFile: Int = R.xml.preferences_backup

    override fun onPreferencesCreated() {

    }

}