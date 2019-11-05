package eu.darken.bb.task.ui.settings

import androidx.annotation.Keep
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.storage.core.StorageSettings
import javax.inject.Inject

@Keep
class TaskSettingsFragment : SmartPreferenceFragment(), AutoInject {

    @Inject lateinit var uiSettings: StorageSettings

    override val settings: StorageSettings by lazy { uiSettings }

    override val preferenceFile: Int = R.xml.preferences_task

    override fun onPreferencesCreated() {

    }

}