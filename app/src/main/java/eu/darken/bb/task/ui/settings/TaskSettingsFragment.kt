package eu.darken.bb.task.ui.settings

import androidx.annotation.Keep
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.task.core.TaskSettings
import javax.inject.Inject

@AndroidEntryPoint
@Keep
class TaskSettingsFragment : SmartPreferenceFragment() {

    @Inject lateinit var taskSettings: TaskSettings

    override val settings: TaskSettings by lazy { taskSettings }

    override val preferenceFile: Int = R.xml.preferences_task

    override fun onPreferencesCreated() {

    }

}