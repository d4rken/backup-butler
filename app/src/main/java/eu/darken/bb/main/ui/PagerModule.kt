package eu.darken.bb.main.ui

import dagger.Module
import dagger.Provides
import eu.darken.bb.R
import eu.darken.bb.main.ui.overview.OverviewFragment
import eu.darken.bb.main.ui.repos.RepoListFragment
import eu.darken.bb.main.ui.schedules.SchedulesFragment
import eu.darken.bb.tasks.ui.tasklist.TaskListFragment

@Module
class PagerModule {

    @Provides
    fun pages(): List<PagerAdapter.Page> = listOf(
            PagerAdapter.Page(OverviewFragment::class, R.string.label_overview),
            PagerAdapter.Page(RepoListFragment::class, R.string.label_repos),
            PagerAdapter.Page(TaskListFragment::class, R.string.label_tasks),
            PagerAdapter.Page(SchedulesFragment::class, R.string.label_schedules)
    )
}