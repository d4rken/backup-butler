package eu.darken.bb.main.ui.settings.debug

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class DebugSettingsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(DebugSettingsFragmentVDC::class)
    abstract fun ui(model: DebugSettingsFragmentVDC.Factory): VDCFactory<out VDC>
}

