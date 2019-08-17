package eu.darken.bb.storage.ui.editor.types.local

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class LocalEditorFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(LocalEditorFragmentVDC::class)
    abstract fun localEditor(model: LocalEditorFragmentVDC.Factory): VDCFactory<out VDC>
}

