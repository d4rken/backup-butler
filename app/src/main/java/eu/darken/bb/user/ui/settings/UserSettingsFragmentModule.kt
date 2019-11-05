package eu.darken.bb.user.ui.settings

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.main.ui.settings.debug.UserSettingsFragmentVDC


@Module
abstract class UserSettingsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(UserSettingsFragmentVDC::class)
    abstract fun ui(model: UserSettingsFragmentVDC.Factory): VDCFactory<out VDC>
}

