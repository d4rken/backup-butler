package eu.darken.bb.backup.ui.generator.list.actions

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class GeneratorsActionDialogModule {
    @Binds
    @IntoMap
    @VDCKey(GeneratorsActionDialogVDC::class)
    abstract fun taskactionVDC(model: GeneratorsActionDialogVDC.Factory): VDCFactory<out VDC>
}

