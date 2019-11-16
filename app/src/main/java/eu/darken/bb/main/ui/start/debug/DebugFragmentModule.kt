package eu.darken.bb.main.ui.start.debug

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class DebugFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(DebugFragmentVDC::class)
    abstract fun debug(model: DebugFragmentVDC.Factory): VDCFactory<out VDC>
}

