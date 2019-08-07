package eu.darken.bb.task.ui.editor.destinations

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class DestinationsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(DestinationsFragmentVDC::class)
    abstract fun destinationsVDC(model: DestinationsFragmentVDC.Factory): VDCFactory<out VDC>
}


