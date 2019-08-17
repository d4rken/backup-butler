package eu.darken.bb.main.ui.start

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.backup.ui.generator.list.GeneratorsFragment
import eu.darken.bb.backup.ui.generator.list.GeneratorsFragmentModule
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.main.ui.start.overview.OverviewFragment
import eu.darken.bb.main.ui.start.overview.OverviewFragmentModule
import eu.darken.bb.schedule.ui.list.SchedulesFragment
import eu.darken.bb.schedule.ui.list.SchedulesFragmentModule
import eu.darken.bb.storage.ui.list.StorageListFragment
import eu.darken.bb.storage.ui.list.StorageListFragmentModule
import eu.darken.bb.task.ui.tasklist.TaskListFragment
import eu.darken.bb.task.ui.tasklist.TaskListFragmentModule

@Module(includes = [PagerModule::class])
abstract class MainActivityModule {

    @Binds
    @IntoMap
    @VDCKey(MainActivityVDC::class)
    abstract fun mainActivityVDC(model: MainActivityVDC.Factory): VDCFactory<out VDC>

    @ContributesAndroidInjector(modules = [OverviewFragmentModule::class])
    abstract fun overviewFragment(): OverviewFragment

    @ContributesAndroidInjector(modules = [StorageListFragmentModule::class])
    abstract fun repolistFragment(): StorageListFragment

    @ContributesAndroidInjector(modules = [TaskListFragmentModule::class])
    abstract fun tasklistFragment(): TaskListFragment

    @ContributesAndroidInjector(modules = [SchedulesFragmentModule::class])
    abstract fun schedulelistFragment(): SchedulesFragment

    @ContributesAndroidInjector(modules = [GeneratorsFragmentModule::class])
    abstract fun generatorListFragment(): GeneratorsFragment
}