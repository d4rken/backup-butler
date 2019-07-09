package eu.darken.bb.main.ui

import dagger.Module
import dagger.Provides
import eu.darken.bb.R
import eu.darken.bb.main.ui.backuplist.BackupListFragment
import eu.darken.bb.main.ui.overview.OverviewFragment
import eu.darken.bb.main.ui.schedules.SchedulesFragment

@Module
class PagerModule {

    @Provides
    fun pages(): List<PagerAdapter.Page> = listOf(
            PagerAdapter.Page(OverviewFragment::class, R.string.label_overview),
            PagerAdapter.Page(BackupListFragment::class, R.string.label_backups),
            PagerAdapter.Page(SchedulesFragment::class, R.string.label_schedules)
    )
}