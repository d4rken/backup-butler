package eu.darken.bb.backup.ui.generator.editor.types.app.preview

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class AppEditorPreviewFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(AppEditorPreviewFragmentVDC::class)
    abstract fun appEditor(model: AppEditorPreviewFragmentVDC.Factory): VDCFactory<out VDC>
}

