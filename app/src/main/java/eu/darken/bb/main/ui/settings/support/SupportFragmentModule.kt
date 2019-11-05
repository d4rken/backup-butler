package eu.darken.bb.main.ui.settings.support

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.main.ui.settings.debug.SupportFragmentVDC


@Module
abstract class SupportFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(SupportFragmentVDC::class)
    abstract fun support(model: SupportFragmentVDC.Factory): VDCFactory<out VDC>
}

