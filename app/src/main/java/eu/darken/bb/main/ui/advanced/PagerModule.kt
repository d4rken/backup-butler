package eu.darken.bb.main.ui.advanced

import dagger.Module
import dagger.Provides
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.list.GeneratorsFragment
import eu.darken.bb.main.ui.advanced.overview.OverviewFragment
import eu.darken.bb.schedule.ui.list.SchedulesFragment
import eu.darken.bb.storage.ui.list.StorageListFragment
import eu.darken.bb.task.ui.tasklist.TaskListFragment

@Module
class PagerModule {

    @Provides
    fun pages(): List<PagerAdapter.Page> {
        val pages = mutableListOf(
            PagerAdapter.Page(OverviewFragment::class, R.string.overview_tab_label),
            PagerAdapter.Page(TaskListFragment::class, R.string.task_tab_label),
            PagerAdapter.Page(StorageListFragment::class, R.string.storage_tab_label),
            PagerAdapter.Page(GeneratorsFragment::class, R.string.backup_generators_label),
            PagerAdapter.Page(SchedulesFragment::class, R.string.scheduler_tab_label)
        )
        return pages.toList()
    }
}