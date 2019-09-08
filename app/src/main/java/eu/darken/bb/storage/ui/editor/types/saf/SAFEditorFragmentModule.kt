package eu.darken.bb.storage.ui.editor.types.saf

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class SAFEditorFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(SAFEditorFragmentVDC::class)
    abstract fun safEditor(model: SAFEditorFragmentVDC.Factory): VDCFactory<out VDC>
}

