package eu.darken.bb.common.file.picker.types

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class TypesPickerFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(TypesPickerFragmentVDC::class)
    abstract fun localPicker(model: TypesPickerFragmentVDC.Factory): VDCFactory<out VDC>
}

