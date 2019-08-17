package eu.darken.bb.onboarding.steps

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class HelloStepFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(HelloStepFragmentVDC::class)
    abstract fun hellostepVDC(model: HelloStepFragmentVDC.Factory): VDCFactory<out VDC>
}

