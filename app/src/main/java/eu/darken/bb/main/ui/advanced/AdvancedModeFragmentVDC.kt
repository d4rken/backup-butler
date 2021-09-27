package eu.darken.bb.main.ui.advanced

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.BackupButler
import eu.darken.bb.R
import eu.darken.bb.backup.ui.generator.list.GeneratorsFragment
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.ReportABug
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.main.ui.advanced.debug.DebugFragment
import eu.darken.bb.main.ui.advanced.overview.OverviewFragment
import eu.darken.bb.schedule.ui.list.SchedulesFragment
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

    val navEvents = SingleLiveEvent<NavDirections>()

    val state = Observable
        .combineLatest(
            bbDebug.observeOptions(),
            uiSettings.showDebugPage.observable
        ) { debug, showDebug ->
            val basePages = listOf(
                PagerAdapter.Page(OverviewFragment::class, R.string.overview_tab_label),
                PagerAdapter.Page(TaskListFragment::class, R.string.task_tab_label),
                PagerAdapter.Page(StorageListFragment::class, R.string.storage_tab_label),
                PagerAdapter.Page(GeneratorsFragment::class, R.string.backup_generators_label),
                PagerAdapter.Page(SchedulesFragment::class, R.string.scheduler_tab_label)
            )
            val pages = if (debug.isDebug() || showDebug) {
                basePages.plus(PagerAdapter.Page(DebugFragment::class, R.string.debug_label))
            } else {
                basePages
            }

            State(
                pages = pages,
                showDebugStuff = showDebug || BBDebug.isDebug(),
                isRecordingDebug = debug.isRecording
            )
        }
        .subscribeOn(Schedulers.computation())
        .toLiveData()

    data class State(
        val pages: List<PagerAdapter.Page>,
        val showDebugStuff: Boolean,
        val isRecordingDebug: Boolean,
    )

    fun switchToSimpleMode() {
        uiSettings.startMode = UISettings.StartMode.SIMPLE
        navEvents.postValue(AdvancedModeFragmentDirections.actionAdvancedModeFragmentToSimpleModeFragment())
    }

    fun reportBug() {
        reportABug.reportABug()
    }

    fun recordDebugLog() {
        bbDebug.setRecording(true)
    }
}