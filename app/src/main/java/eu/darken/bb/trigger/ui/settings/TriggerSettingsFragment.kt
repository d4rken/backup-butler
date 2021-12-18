package eu.darken.bb.trigger.ui.settings

import androidx.annotation.Keep
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.requireActivityActionBar
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.trigger.core.TriggerSettings
import javax.inject.Inject

@Keep
@AndroidEntryPoint
class TriggerSettingsFragment : SmartPreferenceFragment() {

    @Inject lateinit var triggerSettings: TriggerSettings

    override val settings: TriggerSettings by lazy { triggerSettings }

    override val preferenceFile: Int = R.xml.preferences_trigger

    override fun onPreferencesCreated() {
        requireActivityActionBar().setSubtitle(R.string.general_todo_msg)
    }

}