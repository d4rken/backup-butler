package eu.darken.bb.main.ui.newtask

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class NewTaskFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(NewTaskFragmentVDC::class)
    abstract fun exampleFragmentVDC(factory: NewTaskFragmentVDC.Factory): SavedStateVDCFactory<out VDC>
}

