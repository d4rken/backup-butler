package eu.darken.bb.upgrades.ui.settings

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.main.ui.settings.debug.AccountSettingsFragmentVDC


@Module
abstract class AccountSettingsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(AccountSettingsFragmentVDC::class)
    abstract fun ui(model: AccountSettingsFragmentVDC.Factory): VDCFactory<out VDC>
}

