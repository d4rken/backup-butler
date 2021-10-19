package eu.darken.bb.main.ui.advanced

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.BackupButler
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.list.GeneratorListFragment
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.ReportABug
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.asLog
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.main.ui.advanced.debug.DebugFragment
import eu.darken.bb.main.ui.advanced.overview.OverviewFragment
import eu.darken.bb.schedule.ui.list.ScheduleListFragment
import eu.darken.bb.storage.ui.list.StorageListFragment
import eu.darken.bb.task.ui.tasklist.TaskListFragment
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class AdvancedModeFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val reportABug: ReportABug,
    private val uiSettings: UISettings,
    private val bbDebug: BBDebug,
    private val backupButler: BackupButler,
) : VDC() {

    private var currentPosition: Int = 0
    val navEvents = SingleLiveEvent<NavDirections>()

    data class State(
        val pages: List<PagerAdapter.Page>,
        val pagePosition: Int,
        val showDebugStuff: Boolean,
        val isRecordingDebug: Boolean,
    )

    val state = Observable
        .combineLatest(
            bbDebug.observeOptions(),
            uiSettings.showDebugPage.observable
        ) { debug, showDebug ->
            val basePages = listOf(
                PagerAdapter.Page(OverviewFragment::class, R.string.overview_tab_label),
                PagerAdapter.Page(TaskListFragment::class, R.string.task_tab_label),
                PagerAdapter.Page(StorageListFragment::class, R.string.storage_tab_label),
                PagerAdapter.Page(GeneratorListFragment::class, R.string.backup_generators_label),
                PagerAdapter.Page(ScheduleListFragment::class, R.string.scheduler_tab_label)
            )
            val pages = if (debug.isDebug() || showDebug) {
                listOf(PagerAdapter.Page(DebugFragment::class, R.string.debug_label)).plus(basePages)
            } else {
                basePages
            }

            State(
                pages = pages,
                pagePosition = currentPosition,
                showDebugStuff = showDebug || BBDebug.isDebug(),
                isRecordingDebug = debug.isRecording
            )
        }
        .subscribeOn(Schedulers.computation())
        .asLiveData()

    init {
        log { handle.asLog() }
    }

    fun switchUIMode() {
        uiSettings.startMode = UISettings.StartMode.QUICK
        navEvents.postValue(AdvancedModeFragmentDirections.actionNormalModeFragmentToQuickModeFragment())
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