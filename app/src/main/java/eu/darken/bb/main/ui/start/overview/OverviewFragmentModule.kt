package eu.darken.bb.main.ui.start.overview

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class OverviewFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(OverviewFragmentVDC::class)
    abstract fun overviewVDC(model: OverviewFragmentVDC.Factory): VDCFactory<out VDC>
}

