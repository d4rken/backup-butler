package eu.darken.bb.backup.ui.generator.editor.types.files.legacy

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class LegacyFilesEditorFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(LegacyFilesEditorFragmentVDC::class)
    abstract fun filesEditor(model: LegacyFilesEditorFragmentVDC.Factory): VDCFactory<out VDC>
}

