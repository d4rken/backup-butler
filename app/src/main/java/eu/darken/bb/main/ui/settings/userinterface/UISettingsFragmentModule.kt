package eu.darken.bb.main.ui.settings.userinterface

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class UISettingsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(UISettingsFragmentVDC::class)
    abstract fun ui(model: UISettingsFragmentVDC.Factory): VDCFactory<out VDC>
}

