package eu.darken.bb.quickmode.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.BackupButler
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.debug.ReportABug
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.ui.picker.PathPickerOptions
import eu.darken.bb.common.files.ui.picker.PathPickerResult
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.pkgs.picker.ui.PkgPickerOptions
import eu.darken.bb.common.pkgs.picker.ui.PkgPickerResult
import eu.darken.bb.common.smart.Smart2VDC
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
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import kotlinx.coroutines.flow.*
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
    private val storageManager: StorageManager,
    private val quickModeSettings: QuickModeSettings,
    private val processorControl: ProcessorControl,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    val openPathPickerEvent = SingleLiveEvent<PathPickerOptions>()

    val debugState = combine(
        bbDebug.observeOptions(),
        uiSettings.showDebugPage.flow
    ) { debug, showDebug ->
        DebugState(
            showDebugStuff = showDebug || BBDebug.isDebug(),
            isRecordingDebug = debug.isRecording
        )
    }
        .asLiveData2()

    private val appObs: Flow<QuickModeAdapter.Item> = appsQuickMode.appsData.flow
        .onEach { log(TAG) { "Default app task id: $it" } }
        .flatMapLatest { config ->
            storageManager
                .infos(config.storageIds)
                .onStart { emptyList<Storage.InfoOpt>() }
                .map { config to it }
        }
        .map { (appsConfig, storageInfos) ->
            if (appsConfig.isSetUp) {
                QuickAppsVH.Item(
                    config = appsConfig,
                    storageInfos = storageInfos,
                    onView = {
                        QuickModeFragmentDirections.actionQuickModeFragmentToStorageViewerActivity(
                            storageId = it.storageIds.first()
                        ).navVia(this)
                    },
                    onEdit = {
                        QuickModeFragmentDirections.actionQuickModeFragmentToAppsConfigFragment().navVia(this)
                    },
                    onBackup = {
                        QuickModeFragmentDirections.actionQuickModeFragmentToPkgPickerFragment(
                            options = PkgPickerOptions(

                            )
                        ).navVia(this)
                        // TODO launch apps picker
                    },
                    onRestore = {
                        // TODO launch storage browser?
                    }
                )
            } else {
                QuickAppsCreateVH.Item {
                    QuickModeFragmentDirections.actionQuickModeFragmentToAppsConfigFragment().navVia(this)
                }
            }
        }
        .onEach { log(TAG) { "Default app task info: $it" } }
        .onStart { emit(QuickAppsLoadingVH.Item) }

    private val fileObs: Flow<QuickModeAdapter.Item> = filesQuickMode.state.flow
        .onEach { log(TAG) { "File task config: $it" } }
        .flatMapLatest { config ->
            storageManager
                .infos(config.storageIds)
                .onStart { emptyList<Storage.InfoOpt>() }
                .map { config to it }
        }
        .map { (filesConfig, storageInfos) ->
            if (filesConfig.isSetUp) {
                QuickFilesVH.Item(
                    config = filesConfig,
                    storageInfos = storageInfos,
                    onView = {
                        QuickModeFragmentDirections.actionQuickModeFragmentToStorageViewerActivity(
                            storageId = it.storageIds.first()
                        ).navVia(this)
                    },
                    onEdit = {
                        QuickModeFragmentDirections.actionQuickModeFragmentToFilesConfigFragment().navVia(this)
                    },
                    onBackup = {
                        PathPickerOptions(
                            onlyDirs = false,
                            selectionLimit = Int.MAX_VALUE,
                            allowedTypes = setOf(APath.PathType.LOCAL)
                        ).run { openPathPickerEvent.postValue(this) }
                    },
                    onRestore = {
                        // TODO
                    }
                )
            } else {
                QuickFilesCreateVH.Item(
                    onCreateAppsTaskAction = {
                        QuickModeFragmentDirections.actionQuickModeFragmentToFilesConfigFragment().navVia(this)
                    }
                )
            }
        }
        .onEach { log(TAG) { "Default file task info: $it" } }
        .onStart { emit(QuickFilesLoadingVH.Item) }

    val items: LiveData<List<QuickModeAdapter.Item>> = combine(
        appObs,
        fileObs,
        quickModeSettings.isHintAdvancedModeDismissed.flow
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
        .asLiveData2()

    val processorState: LiveData<Boolean> = processorControl.progressHost
        .map { it != null }
        .asLiveData2()

    fun onAppsPickerResult(result: PkgPickerResult?) = launch {
        log(TAG) { "onAppsPickerResult(result=$result)" }
        if (result?.isSuccess != true) return@launch

        appsQuickMode.launchBackup(result.selection!!)
    }

    fun onPathPickerResult(result: PathPickerResult) = launch {
        log(TAG) { "onPathPickerResult(result=$result)" }
        if (!result.isSuccess) return@launch

        filesQuickMode.launchBackup(result.selection!!)
    }

    data class DebugState(
        val showDebugStuff: Boolean,
        val isRecordingDebug: Boolean
    )

    fun switchToAdvancedMode() {
        uiSettings.startMode = UISettings.StartMode.NORMAL
        QuickModeFragmentDirections.actionQuickModeFragmentToMainFragment().navVia(this)
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