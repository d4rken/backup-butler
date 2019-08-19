package eu.darken.bb.task.ui.editor.backup.sources

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class SourcesFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(SourcesFragmentVDC::class)
    abstract fun sourcesVDC(model: SourcesFragmentVDC.Factory): VDCFactory<out VDC>
}


