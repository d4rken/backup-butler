package eu.darken.bb.main.ui

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey
import eu.darken.bb.main.ui.overview.OverviewFragment
import eu.darken.bb.main.ui.overview.OverviewFragmentModule
import eu.darken.bb.main.ui.schedules.SchedulesFragment
import eu.darken.bb.main.ui.schedules.SchedulesFragmentModule
import eu.darken.bb.repos.ui.list.RepoListFragment
import eu.darken.bb.repos.ui.list.RepoListFragmentModule
import eu.darken.bb.tasks.ui.tasklist.TaskListFragment
import eu.darken.bb.tasks.ui.tasklist.TaskListFragmentModule

@Module(includes = [PagerModule::class])
abstract class MainActivityModule {

    @Binds
    @IntoMap
    @VDCKey(MainActivityVDC::class)
    abstract fun mainActivityVDC(model: MainActivityVDC.Factory): VDCFactory<out VDC>

    @ContributesAndroidInjector(modules = [OverviewFragmentModule::class])
    abstract fun overviewFragment(): OverviewFragment

    @ContributesAndroidInjector(modules = [RepoListFragmentModule::class])
    abstract fun repolistFragment(): RepoListFragment

    @ContributesAndroidInjector(modules = [TaskListFragmentModule::class])
    abstract fun tasklistFragment(): TaskListFragment

    @ContributesAndroidInjector(modules = [SchedulesFragmentModule::class])
    abstract fun schedulelistFragment(): SchedulesFragment
}