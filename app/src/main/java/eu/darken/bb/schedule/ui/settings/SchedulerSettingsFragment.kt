package eu.darken.bb.schedule.ui.settings

import androidx.annotation.Keep
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.schedule.core.SchedulerSettings
import javax.inject.Inject

@Keep
@AndroidEntryPoint
class SchedulerSettingsFragment : SmartPreferenceFragment() {

    @Inject lateinit var schedulerSettings: SchedulerSettings

    override val settings: SchedulerSettings by lazy { schedulerSettings }

    override val preferenceFile: Int = R.xml.preferences_scheduler

    override fun onPreferencesCreated() {

    }

}