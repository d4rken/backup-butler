package eu.darken.bb.storage.ui.editor.types.local

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class LocalEditorFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(LocalEditorFragmentVDC::class)
    abstract fun localEditor(model: LocalEditorFragmentVDC.Factory): VDCFactory<out VDC>
}

