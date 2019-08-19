package eu.darken.bb.task.ui.editor.backup.destinations

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class DestinationsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(DestinationsFragmentVDC::class)
    abstract fun destinationsVDC(model: DestinationsFragmentVDC.Factory): VDCFactory<out VDC>
}


