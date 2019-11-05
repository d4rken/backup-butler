package eu.darken.bb.main.ui.settings.debug

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class GeneralSettingsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(GeneralSettingsFragmentVDC::class)
    abstract fun ui(model: GeneralSettingsFragmentVDC.Factory): VDCFactory<out VDC>
}

