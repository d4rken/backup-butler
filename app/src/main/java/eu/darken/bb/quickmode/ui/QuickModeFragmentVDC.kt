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
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.ui.picker.PathPicker
import eu.darken.bb.common.navigation.NavEventsSource
import eu.darken.bb.common.navigation.via
import eu.darken.bb.common.pkgpicker.ui.PkgPickerOptions
import eu.darken.bb.common.pkgpicker.ui.PkgPickerResult
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.quickmode.core.AppsQuickMode
import eu.darken.bb.quickmode.core.FilesQuickMode
import eu.darken.bb.quickmode.core.QuickModeSettings
import eu.darken.bb.quickmode.ui.apps.QuickAppsCreateVH
import eu.darken.bb.quickmode.ui.apps.QuickAppsLoadingVH
import eu.darken.bb.quickmode.ui.apps.QuickAppsVH
import eu.darken.bb.quickmode.ui.common.AdvancedModeHintsVH
import eu.darken.bb.quickmode.ui.files.QuickFilesCreateVH
import eu.darken.bb.quickmode.ui.files.QuickFilesLoadingVH
import eu.darken.bb.quickmode.ui.files.QuickFilesVH
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.core.StorageRefRepo
import eu.darken.bb.storage.ui.picker.StoragePickerResult
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
    private val appsQuickMode: AppsQuickMode,
    private val filesQuickMode: FilesQuickMode,
    private val taskRepo: TaskRepo,
    private val storageRefRepo: StorageRefRepo,
    private val storageManager: StorageManager,
    private val quickModeSettings: QuickModeSettings,
    private val processorControl: ProcessorControl,
) : SmartVDC(), NavEventsSource {

    override val navEvents = SingleLiveEvent<NavDirections>()
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

    private val appObs: Observable<out QuickModeAdapter.Item> = appsQuickMode.appsData.data
        .observeOn(Schedulers.computation())
        .doOnNext { log(TAG) { "Default app task id: $it" } }
        .map { appsConfig ->
            if (appsConfig.isSetUp) {
                QuickAppsVH.Item(
                    config = appsConfig,
                    onView = {
                        QuickModeFragmentDirections.actionQuickModeFragmentToStorageViewerActivity(
                            storageId = it.storageIds.first()
                        ).via(this)
                    },
                    onEdit = {
                        QuickModeFragmentDirections.actionQuickModeFragmentToAppsConfigFragment().via(this)
                    },
                    onBackup = {
                        QuickModeFragmentDirections.actionQuickModeFragmentToPkgPickerFragment(
                            options = PkgPickerOptions(

                            )
                        ).via(this)
                        // TODO launch apps picker
                    },
                    onRestore = {
                        // TODO launch storage browser?
                    }
                )
            } else {
                QuickAppsCreateVH.Item {
                    QuickModeFragmentDirections.actionQuickModeFragmentToAppsConfigFragment().via(this)
                }
            }
        }
        .doOnNext { log(TAG) { "Default app task info: $it" } }
        .startWithItem(QuickAppsLoadingVH.Item)

    private val fileObs: Observable<out QuickModeAdapter.Item> = filesQuickMode.hotData.data
        .observeOn(Schedulers.computation())
        .doOnNext { log(TAG) { "Default file task id: $it" } }
        .map { filesConfig ->
            if (filesConfig.isSetUp) {
                QuickFilesVH.Item(
                    config = filesConfig,
                    onView = {
                        QuickModeFragmentDirections.actionQuickModeFragmentToStorageViewerActivity(
                            storageId = it.storageIds.first()
                        ).via(this)
                    },
                    onEdit = {
                        QuickModeFragmentDirections.actionQuickModeFragmentToFilesConfigFragment().via(this)
                    },
                    onBackup = {
                        // TODO Launch file picker
                        QuickModeFragmentDirections.actionQuickModeFragmentToPathPickerActivity(
                            options = PathPicker.Options(
                                onlyDirs = false,
                                selectionLimit = Int.MAX_VALUE,
                                allowedTypes = setOf(APath.PathType.LOCAL)
                            )
                        ).via(this)
                    },
                    onRestore = {
                        // TODO launch storage browser?
                    }
                )
            } else {
                QuickFilesCreateVH.Item(
                    onCreateAppsTaskAction = {
                        QuickModeFragmentDirections.actionQuickModeFragmentToFilesConfigFragment().via(this)
                    }
                )
            }
        }
        .doOnNext { log(TAG) { "Default file task info: $it" } }
        .startWithItem(QuickFilesLoadingVH.Item)

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

    val processorState: LiveData<Boolean> = processorControl.progressHost
        .map { it.isNotNull }
        .asLiveData()

    fun onAppsPickerResult(result: PkgPickerResult?) {
        log(TAG) { "onAppsPickerResult(result=$result)" }
        if (result == null) return

        // TODO launch quick mode single shoot apps backup
    }

    fun onPathPickerResult(result: StoragePickerResult?) {
        log(TAG) { "onPathPickerResult(result=$result)" }
        if (result == null) return

        // TODO launch quick mode single shoot files backup
    }

    data class DebugState(
        val showDebugStuff: Boolean,
        val isRecordingDebug: Boolean
    )

    fun switchToAdvancedMode() {
        uiSettings.startMode = UISettings.StartMode.NORMAL
        QuickModeFragmentDirections.actionQuickModeFragmentToMainFragment().via(this)
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