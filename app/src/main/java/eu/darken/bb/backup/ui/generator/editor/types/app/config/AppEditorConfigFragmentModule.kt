package eu.darken.bb.backup.ui.generator.editor.types.app.config

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class AppEditorConfigFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(AppEditorConfigFragmentVDC::class)
    abstract fun appEditor(model: AppEditorConfigFragmentVDC.Factory): VDCFactory<out VDC>
}

