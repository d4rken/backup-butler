package eu.darken.bb.settings.ui.userinterface

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class UISettingsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(UISettingsFragmentVDC::class)
    abstract fun ui(model: UISettingsFragmentVDC.Factory): VDCFactory<out VDC>
}

