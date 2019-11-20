package eu.darken.bb.main.ui.simple.start

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class StartFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(StartFragmentVDC::class)
    abstract fun startVDC(model: StartFragmentVDC.Factory): VDCFactory<out VDC>
}

