package eu.darken.bb.main.ui

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.dagger.VDCKey
import eu.darken.bb.main.ui.backuplist.BackupListFragment
import eu.darken.bb.main.ui.backuplist.BackupListFragmentModule
import eu.darken.bb.main.ui.overview.OverviewFragment
import eu.darken.bb.main.ui.overview.OverviewFragmentModule
import eu.darken.bb.main.ui.schedules.SchedulesFragment
import eu.darken.bb.main.ui.schedules.SchedulesFragmentModule

@Module(includes = [PagerModule::class])
abstract class MainActivityModule {

    @Binds
    @IntoMap
    @VDCKey(MainActivityVDC::class)
    abstract fun mainActivityVDC(model: MainActivityVDC.Factory): SavedStateVDCFactory<out VDC>

    @ContributesAndroidInjector(modules = [OverviewFragmentModule::class])
    abstract fun overviewFragment(): OverviewFragment

    @ContributesAndroidInjector(modules = [BackupListFragmentModule::class])
    abstract fun backuplistFragment(): BackupListFragment

    @ContributesAndroidInjector(modules = [SchedulesFragmentModule::class])
    abstract fun tasklistFragment(): SchedulesFragment
}