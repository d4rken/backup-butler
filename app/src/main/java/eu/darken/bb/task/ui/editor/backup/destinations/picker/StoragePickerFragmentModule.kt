package eu.darken.bb.task.ui.editor.backup.destinations.picker

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class StoragePickerFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(StoragePickerFragmentVDC::class)
    abstract fun storagePickerVDC(model: StoragePickerFragmentVDC.Factory): VDCFactory<out VDC>
}


