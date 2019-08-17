package eu.darken.bb.schedule.ui.list

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class SchedulesFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(SchedulesFragmentVDC::class)
    abstract fun overviewVDC(model: SchedulesFragmentVDC.Factory): VDCFactory<out VDC>
}

