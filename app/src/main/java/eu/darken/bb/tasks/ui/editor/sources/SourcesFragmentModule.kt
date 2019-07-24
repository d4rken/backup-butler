package eu.darken.bb.tasks.ui.editor.sources

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class SourcesFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(SourcesFragmentVDC::class)
    abstract fun sourcesVDC(model: SourcesFragmentVDC.Factory): VDCFactory<out VDC>
}


