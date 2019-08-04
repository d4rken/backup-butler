package eu.darken.bb.backups.ui.generator.editor.types.app

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class AppEditorFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(AppEditorFragmentVDC::class)
    abstract fun appEditor(model: AppEditorFragmentVDC.Factory): VDCFactory<out VDC>
}

