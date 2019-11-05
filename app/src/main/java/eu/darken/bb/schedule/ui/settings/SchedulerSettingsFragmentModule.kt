package eu.darken.bb.schedule.ui.settings

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class SchedulerSettingsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(SchedulerSettingsFragmentVDC::class)
    abstract fun scheduler(model: SchedulerSettingsFragmentVDC.Factory): VDCFactory<out VDC>
}

