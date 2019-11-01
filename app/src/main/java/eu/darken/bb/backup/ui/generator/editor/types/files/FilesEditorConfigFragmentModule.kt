package eu.darken.bb.backup.ui.generator.editor.types.files

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class FilesEditorConfigFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(FilesEditorConfigFragmentVDC::class)
    abstract fun filesEditor(model: FilesEditorConfigFragmentVDC.Factory): VDCFactory<out VDC>
}

