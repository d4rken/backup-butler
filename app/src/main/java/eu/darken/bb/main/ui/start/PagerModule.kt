package eu.darken.bb.main.ui.start

import dagger.Module
import dagger.Provides
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.list.GeneratorsFragment
import eu.darken.bb.main.ui.start.overview.OverviewFragment
import eu.darken.bb.schedule.ui.list.SchedulesFragment
import eu.darken.bb.storage.ui.list.StorageListFragment
import eu.darken.bb.task.ui.tasklist.TaskListFragment

@Module
class PagerModule {

    @Provides
    fun pages(): List<PagerAdapter.Page> = listOf(
            PagerAdapter.Page(OverviewFragment::class, R.string.label_overview),
            PagerAdapter.Page(StorageListFragment::class, R.string.label_repos),
            PagerAdapter.Page(TaskListFragment::class, R.string.label_tasks),
            PagerAdapter.Page(SchedulesFragment::class, R.string.label_schedules),
            PagerAdapter.Page(GeneratorsFragment::class, R.string.label_sources)
    )
}