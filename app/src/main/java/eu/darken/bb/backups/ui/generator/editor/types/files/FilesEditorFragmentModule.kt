package eu.darken.bb.backups.ui.generator.editor.types.files

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class FilesEditorFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(FilesEditorFragmentVDC::class)
    abstract fun filesEditor(model: FilesEditorFragmentVDC.Factory): VDCFactory<out VDC>
}

