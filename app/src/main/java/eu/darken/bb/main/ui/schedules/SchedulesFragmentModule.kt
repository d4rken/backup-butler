package eu.darken.bb.main.ui.schedules

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class SchedulesFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(SchedulesFragmentVDC::class)
    abstract fun overviewVDC(model: SchedulesFragmentVDC.Factory): VDCFactory<out VDC>
}

