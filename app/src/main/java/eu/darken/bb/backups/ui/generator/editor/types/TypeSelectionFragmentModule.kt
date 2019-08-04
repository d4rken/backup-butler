package eu.darken.bb.backups.ui.generator.editor.types

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class TypeSelectionFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(TypeSelectionFragmentVDC::class)
    abstract fun typeSelection(model: TypeSelectionFragmentVDC.Factory): VDCFactory<out VDC>
}

