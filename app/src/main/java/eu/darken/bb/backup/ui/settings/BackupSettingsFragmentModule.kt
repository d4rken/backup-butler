package eu.darken.bb.backup.ui.settings

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class BackupSettingsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(BackupSettingsFragmentVDC::class)
    abstract fun backup(model: BackupSettingsFragmentVDC.Factory): VDCFactory<out VDC>
}

