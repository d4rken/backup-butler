package eu.darken.bb.backup.ui.settings

import androidx.annotation.Keep
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.backup.core.BackupSettings
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartPreferenceFragment
import javax.inject.Inject

@Keep
@AndroidEntryPoint
class BackupSettingsFragment : SmartPreferenceFragment() {

    @Inject lateinit var uiSettings: BackupSettings

    override val settings: BackupSettings by lazy { uiSettings }

    override val preferenceFile: Int = R.xml.preferences_backup

    override fun onPreferencesCreated() {
        requireActivityActionBar().setSubtitle(R.string.general_todo_msg)
    }

}