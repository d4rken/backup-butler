package eu.darken.bb.processor.ui.progress

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class ProgressFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(ProgressFragmentVDC::class)
    abstract fun progress(model: ProgressFragmentVDC.Factory): VDCFactory<out VDC>
}

