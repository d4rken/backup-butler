package eu.darken.bb.main.ui.simple

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
import eu.darken.bb.main.core.simple.SimpleMode
import eu.darken.bb.main.core.simple.SimpleModeSettings
import eu.darken.bb.main.ui.simple.cards.apps.AppsInfoCreateVH
import eu.darken.bb.main.ui.simple.cards.apps.AppsInfoLoadingVH
import eu.darken.bb.main.ui.simple.cards.files.FilesInfoCreateVH
import eu.darken.bb.main.ui.simple.cards.files.FilesInfoLoadingVH
import eu.darken.bb.main.ui.simple.cards.hints.AdvancedModeHintsVH
import eu.darken.bb.main.ui.simple.cards.info.BBInfoLoadingVH
import eu.darken.bb.main.ui.simple.cards.info.BBInfoVH
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.core.StorageRefRepo
import eu.darken.bb.task.core.TaskRepo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class SimpleModeFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val reportABug: ReportABug,
    private val uiSettings: UISettings,
    private val bbDebug: BBDebug,
    private val backupButler: BackupButler,
    private val simpleMode: SimpleMode,
    private val taskRepo: TaskRepo,
    private val storageRefRepo: StorageRefRepo,
    private val storageManager: StorageManager,
    private val simpleModeSettings: SimpleModeSettings,
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

    private val infoObs: Observable<out SimpleModeAdapter.Item> = Observable
        .create<SimpleModeAdapter.Item> {
            val item = BBInfoVH.Item(
                appInfo = backupButler.appInfo,
                onUpgradeAction = {
                    TODO()
                }
            )
            it.onNext(item)
        }
        .subscribeOn(Schedulers.computation())
        .startWithItem(BBInfoLoadingVH.Item)

    private val appObs: Observable<out SimpleModeAdapter.Item> = simpleMode.appsData
        .observeOn(Schedulers.computation())
        .doOnNext { log(TAG) { "Default app task id: $it" } }
        .flatMap { appsData ->
            if (appsData.taskId == null) {
                AppsInfoCreateVH.Item {
                    navEvents.postValue(SimpleModeFragmentDirections.actionSimpleModeFragmentToWizardAppsFragment())
                }.let { Observable.just(it) }
            } else {
                taskRepo.get(appsData.taskId).toObservable()
            } as Observable<out SimpleModeAdapter.Item>
        }
        .doOnNext { log(TAG) { "Default app task info: $it" } }
        .startWithItem(AppsInfoLoadingVH.Item)

    private val fileObs: Observable<out SimpleModeAdapter.Item> = simpleMode.filesData
        .observeOn(Schedulers.computation())
        .doOnNext { log(TAG) { "Default file task id: $it" } }
        .flatMap { filesData ->
            if (filesData.taskId == null) {
                FilesInfoCreateVH.Item {
                    navEvents.postValue(SimpleModeFragmentDirections.actionSimpleModeFragmentToWizardFilesFragment())
                }.let { Observable.just(it) }
            } else {
                taskRepo.get(filesData.taskId).toObservable()
            } as Observable<out SimpleModeAdapter.Item>
        }
        .doOnNext { log(TAG) { "Default file task info: $it" } }
        .startWithItem(FilesInfoLoadingVH.Item)

    val items: LiveData<List<SimpleModeAdapter.Item>> = Observable
        .combineLatest(
            infoObs,
            appObs,
            fileObs,
            simpleModeSettings.isHintAdvancedModeDismissed.observable.observeOn(Schedulers.computation())
        ) { info, apps, files, isHintAdvModeDismissed ->
            mutableListOf(info, apps, files).apply {
                if (!isHintAdvModeDismissed) {
                    val hint = AdvancedModeHintsVH.Item(
                        onDismiss = {
                            simpleModeSettings.isHintAdvancedModeDismissed.update { true }
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
        uiSettings.startMode = UISettings.StartMode.ADVANCED
        navEvents.postValue(SimpleModeFragmentDirections.actionSimpleModeFragmentToAdvancedModeFragment())
    }

    fun reportBug() {
        reportABug.reportABug()
    }

    fun recordDebugLog() {
        bbDebug.setRecording(true)
    }

    companion object {
        private val TAG = logTag("SimpleMode", "Main", "VDC")
    }
}