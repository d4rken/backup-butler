package eu.darken.bb.schedule.ui.settings

import androidx.annotation.Keep
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartPreferenceFragment
import eu.darken.bb.schedule.core.SchedulerSettings
import javax.inject.Inject

@Keep
class SchedulerSettingsFragment : SmartPreferenceFragment(), AutoInject {

    @Inject lateinit var schedulerSettings: SchedulerSettings

    override val settings: SchedulerSettings by lazy { schedulerSettings }

    override val preferenceFile: Int = R.xml.preferences_scheduler

    override fun onPreferencesCreated() {

    }

}