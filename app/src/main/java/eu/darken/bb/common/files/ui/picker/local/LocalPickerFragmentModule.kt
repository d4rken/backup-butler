package eu.darken.bb.common.files.ui.picker.local

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class LocalPickerFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(LocalPickerFragmentVDC::class)
    abstract fun localPicker(model: LocalPickerFragmentVDC.Factory): VDCFactory<out VDC>
}

