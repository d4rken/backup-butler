package eu.darken.bb.backup.ui.generator.editor.types.files

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class FilesEditorFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(FilesEditorFragmentVDC::class)
    abstract fun filesEditor(model: FilesEditorFragmentVDC.Factory): VDCFactory<out VDC>
}

