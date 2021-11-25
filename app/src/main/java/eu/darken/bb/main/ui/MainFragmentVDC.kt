package eu.darken.bb.main.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.BackupButler
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.list.GeneratorListFragment
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.ReportABug
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.common.vdc.asLog
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.main.ui.debug.DebugFragment
import eu.darken.bb.main.ui.overview.OverviewFragment
import eu.darken.bb.schedule.ui.list.TriggerListFragment
import eu.darken.bb.storage.ui.list.StorageListFragment
import eu.darken.bb.task.ui.tasklist.TaskListFragment
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class MainFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val reportABug: ReportABug,
    private val uiSettings: UISettings,
    private val bbDebug: BBDebug,
    private val backupButler: BackupButler,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private var currentPosition: Int = 0

    data class State(
        val pages: List<MainPagerAdapter.Page>,
        val pagePosition: Int,
        val showDebugStuff: Boolean,
        val isRecordingDebug: Boolean,
    )

    val state = combine(
        bbDebug.observeOptions(),
        uiSettings.showDebugPage.flow
    ) { debug, showDebug ->
        val basePages = listOf(
            MainPagerAdapter.Page(OverviewFragment::class, R.string.overview_tab_label),
            MainPagerAdapter.Page(TaskListFragment::class, R.string.task_tab_label),
            MainPagerAdapter.Page(StorageListFragment::class, R.string.storage_tab_label),
            MainPagerAdapter.Page(GeneratorListFragment::class, R.string.backup_generators_label),
            MainPagerAdapter.Page(TriggerListFragment::class, R.string.trigger_tab_label)
        )
        val pages = if (debug.isDebug() || showDebug) {
            listOf(MainPagerAdapter.Page(DebugFragment::class, R.string.debug_label)).plus(basePages)
        } else {
            basePages
        }

        State(
            pages = pages,
            pagePosition = currentPosition,
            showDebugStuff = showDebug || BBDebug.isDebug(),
            isRecordingDebug = debug.isRecording
        )
    }.asLiveData2()

    init {
        log { handle.asLog() }
    }

    fun switchUIMode() {
        uiSettings.startMode = UISettings.StartMode.QUICK
        navEvents.postValue(MainFragmentDirections.actionMainFragmentToQuickModeFragment())
    }

    fun reportBug() {
        reportABug.reportABug()
    }

    fun recordDebugLog() {
        bbDebug.setRecording(true)
    }

    fun updateCurrentPage(position: Int) {
        currentPosition = position
    }
}