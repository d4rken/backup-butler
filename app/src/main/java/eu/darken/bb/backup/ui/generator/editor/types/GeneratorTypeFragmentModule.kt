package eu.darken.bb.backup.ui.generator.editor.types

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class GeneratorTypeFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(GeneratorTypeFragmentVDC::class)
    abstract fun typeSelection(model: GeneratorTypeFragmentVDC.Factory): VDCFactory<out VDC>
}

