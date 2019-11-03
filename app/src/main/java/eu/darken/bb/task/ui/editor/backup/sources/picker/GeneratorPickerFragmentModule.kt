package eu.darken.bb.task.ui.editor.backup.sources.picker

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class GeneratorPickerFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(GeneratorPickerFragmentVDC::class)
    abstract fun sourcePicker(model: GeneratorPickerFragmentVDC.Factory): VDCFactory<out VDC>
}


