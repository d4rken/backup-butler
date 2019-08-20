package eu.darken.bb.task.ui.editor.restore.sources

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class RestoreSourcesFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(RestoreSourcesFragmentVDC::class)
    abstract fun restoreSources(model: RestoreSourcesFragmentVDC.Factory): VDCFactory<out VDC>
}

