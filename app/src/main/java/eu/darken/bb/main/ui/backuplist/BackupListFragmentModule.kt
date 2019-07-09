package eu.darken.bb.main.ui.backuplist

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class BackupListFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(BackupListFragmentVDC::class)
    abstract fun overviewVDC(model: BackupListFragmentVDC.Factory): SavedStateVDCFactory<out VDC>
}

