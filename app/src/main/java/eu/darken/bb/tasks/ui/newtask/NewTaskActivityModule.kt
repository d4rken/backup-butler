package eu.darken.bb.tasks.ui.newtask

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey

@Module
class NewTaskActivityModule {

    @PerActivity
    @Provides
    @IntoMap
    @VDCKey(NewTaskActivityVDC::class)
    fun taskActivity(factory: NewTaskActivityVDC.Factory): VDCFactory<out VDC> {
        return factory
    }



}