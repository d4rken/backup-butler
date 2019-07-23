package eu.darken.bb.onboarding.steps

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class HelloStepFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(HelloStepFragmentVDC::class)
    abstract fun hellostepVDC(model: HelloStepFragmentVDC.Factory): VDCFactory<out VDC>
}

