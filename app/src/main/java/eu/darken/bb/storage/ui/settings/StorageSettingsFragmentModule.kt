package eu.darken.bb.storage.ui.settings

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class StorageSettingsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(StorageSettingsFragmentVDC::class)
    abstract fun ui(model: StorageSettingsFragmentVDC.Factory): VDCFactory<out VDC>
}

