package eu.darken.bb.main.ui.advanced

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.backup.ui.generator.list.GeneratorsFragment
import eu.darken.bb.backup.ui.generator.list.GeneratorsFragmentModule
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.main.ui.advanced.debug.DebugFragment
import eu.darken.bb.main.ui.advanced.debug.DebugFragmentModule
import eu.darken.bb.main.ui.advanced.overview.OverviewFragment
import eu.darken.bb.main.ui.advanced.overview.OverviewFragmentModule
import eu.darken.bb.schedule.ui.list.SchedulesFragment
import eu.darken.bb.schedule.ui.list.SchedulesFragmentModule
import eu.darken.bb.storage.ui.list.StorageListFragment
import eu.darken.bb.storage.ui.list.StorageListFragmentModule
import eu.darken.bb.task.ui.tasklist.TaskListFragment
import eu.darken.bb.task.ui.tasklist.TaskListFragmentModule

@Module(includes = [PagerModule::class])
abstract class AdvancedActivityModule {

    @Binds
    @IntoMap
    @VDCKey(AdvancedActivityVDC::class)
    abstract fun mainActivityVDC(model: AdvancedActivityVDC.Factory): VDCFactory<out VDC>

    @ContributesAndroidInjector(modules = [DebugFragmentModule::class])
    abstract fun debugFragment(): DebugFragment

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