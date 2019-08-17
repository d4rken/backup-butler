package eu.darken.bb.backup.ui.generator.editor.types.app

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class AppEditorFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(AppEditorFragmentVDC::class)
    abstract fun appEditor(model: AppEditorFragmentVDC.Factory): VDCFactory<out VDC>
}

