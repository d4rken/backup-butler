package eu.darken.bb.main.ui.settings

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.preference.Preference
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.BackupButler
import eu.darken.bb.GeneralSettings
import eu.darken.bb.R
import eu.darken.bb.common.WebpageTool
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.main.core.Settings
import javax.inject.Inject

@AndroidEntryPoint
class IndexFragment : SmartPreferenceFragment() {

    @Inject lateinit var generalSettings: GeneralSettings
    override val settings: Settings by lazy { generalSettings }
    override val preferenceFile: Int = R.xml.preferences_index

    @Inject lateinit var backupButler: BackupButler
    @Inject lateinit var webpageTool: WebpageTool

    init {
        setHasOptionsMenu(true)
    }

    override fun onPreferencesCreated() {
        findPreference<Preference>("core.changelog")!!.summary = backupButler.appInfo.fullVersionString

        super.onPreferencesCreated()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_settings_index, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_item_twitter -> {
            webpageTool.open("https://bb.darken.eu/twitter")
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}