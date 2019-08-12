package eu.darken.bb.settings.ui.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class UIPrefFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(UIPrefFragmentVDC::class)
    abstract fun ui(model: UIPrefFragmentVDC.Factory): VDCFactory<out VDC>
}

