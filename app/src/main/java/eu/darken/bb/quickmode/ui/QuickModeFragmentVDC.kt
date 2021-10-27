package eu.darken.bb.quickmode.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.BackupButler
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.ReportABug
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.quickmode.core.QuickModeRepo
import eu.darken.bb.quickmode.core.QuickModeSettings
import eu.darken.bb.quickmode.ui.cards.apps.AppsInfoCreateVH
import eu.darken.bb.quickmode.ui.cards.apps.AppsInfoLoadingVH
import eu.darken.bb.quickmode.ui.cards.files.FilesInfoCreateVH
import eu.darken.bb.quickmode.ui.cards.files.FilesInfoLoadingVH
import eu.darken.bb.quickmode.ui.cards.files.FilesInfoVH
import eu.darken.bb.quickmode.ui.cards.hints.AdvancedModeHintsVH
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.core.StorageRefRepo
import eu.darken.bb.task.core.TaskRepo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class QuickModeFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val reportABug: ReportABug,
    private val uiSettings: UISettings,
    private val bbDebug: BBDebug,
    private val backupButler: BackupButler,
    private val quickModeRepo: QuickModeRepo,
    private val taskRepo: TaskRepo,
    private val storageRefRepo: StorageRefRepo,
    private val storageManager: StorageManager,
    private val quickModeSettings: QuickModeSettings,
) : SmartVDC() {

    val navEvents = SingleLiveEvent<NavDirections>()
    val debugState = Observable
        .combineLatest(
            bbDebug.observeOptions(),
            uiSettings.showDebugPage.observable
        ) { debug, showDebug ->
            DebugState(
                showDebugStuff = showDebug || BBDebug.isDebug(),
                isRecordingDebug = debug.isRecording
            )
        }
        .subscribeOn(Schedulers.computation())
        .asLiveData()

//    private val storageObs: Observable<out SimpleModeAdapter.Item> = simpleMode.data
//        .observeOn(Schedulers.computation())
//        .map { it.storageIds }
//        .flatMap { storageManager.infos(it) }
//        .doOnNext { log(TAG) { "Default storage infos: $it" } }

    private val appObs: Observable<out QuickModeAdapter.Item> = quickModeRepo.appsData.data
        .observeOn(Schedulers.computation())
        .doOnNext { log(TAG) { "Default app task id: $it" } }
        .flatMap { appsData ->
            if (appsData.taskId == null) {
                AppsInfoCreateVH.Item {
                    navEvents.postValue(QuickModeFragmentDirections.actionQuickModeFragmentToWizardAppsFragment())
                }.let { Observable.just(it) }
            } else {
                taskRepo.get(appsData.taskId).toObservable()
            } as Observable<out QuickModeAdapter.Item>
        }
        .doOnNext { log(TAG) { "Default app task info: $it" } }
        .startWithItem(AppsInfoLoadingVH.Item)

    private val fileObs: Observable<out QuickModeAdapter.Item> = quickModeRepo.filesData.data
        .observeOn(Schedulers.computation())
        .doOnNext { log(TAG) { "Default file task id: $it" } }
        .flatMap { filesData ->
            if (filesData.taskId == null) {
                FilesInfoCreateVH.Item(
                    onCreateAppsTaskAction = {
                        navEvents.postValue(QuickModeFragmentDirections.actionQuickModeFragmentToWizardFilesFragment())
                    }
                ).let { Observable.just(it) }
            } else {
                taskRepo.get(filesData.taskId)
                    .toObservable()
                    .map { task ->
                        FilesInfoVH.Item(
                            task = task,
                            onBackup = {

                            },
                            onEdit = {
                                QuickModeFragmentDirections.actionQuickModeFragmentToWizardFilesFragment(
                                    taskId = it
                                ).run { navEvents.postValue(this) }
                            },
                            onView = {

                            },
                            onRestore = {

                            }
                        )
                    }
            }
        }
        .doOnNext { log(TAG) { "Default file task info: $it" } }
        .startWithItem(FilesInfoLoadingVH.Item)

    val items: LiveData<List<QuickModeAdapter.Item>> = Observable
        .combineLatest(
            appObs,
            fileObs,
            quickModeSettings.isHintAdvancedModeDismissed.observable.observeOn(Schedulers.computation())
        ) { apps, files, isHintAdvModeDismissed ->
            mutableListOf(apps, files).apply {
                if (!isHintAdvModeDismissed) {
                    val hint = AdvancedModeHintsVH.Item(
                        onDismiss = {
                            quickModeSettings.isHintAdvancedModeDismissed.update { true }
                        }
                    )
                    add(0, hint)
                }
            }.toList()
        }
        .asLiveData()

    data class DebugState(
        val showDebugStuff: Boolean,
        val isRecordingDebug: Boolean
    )

    fun switchToAdvancedMode() {
        uiSettings.startMode = UISettings.StartMode.NORMAL
        navEvents.postValue(QuickModeFragmentDirections.actionQuickModeFragmentToMainFragment())
    }

    fun reportBug() {
        reportABug.reportABug()
    }

    fun recordDebugLog() {
        bbDebug.setRecording(true)
    }

    companion object {
        private val TAG = logTag("QuickMode", "Main", "VDC")
    }
}