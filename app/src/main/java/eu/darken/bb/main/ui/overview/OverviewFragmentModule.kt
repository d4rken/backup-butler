package eu.darken.bb.main.ui.overview

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class OverviewFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(OverviewFragmentVDC::class)
    abstract fun overviewVDC(model: OverviewFragmentVDC.Factory): VDCFactory<out VDC>
}

