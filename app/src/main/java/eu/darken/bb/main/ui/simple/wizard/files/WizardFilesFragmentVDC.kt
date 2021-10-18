package eu.darken.bb.main.ui.simple.wizard.files

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import com.jakewharton.rx3.replayingShare
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.main.core.simple.AutoSetUp
import eu.darken.bb.main.core.simple.SimpleMode
import eu.darken.bb.main.ui.simple.wizard.apps.WizardAppsFragmentVDC
import eu.darken.bb.main.ui.simple.wizard.common.*
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.picker.StoragePickerResult
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class WizardFilesFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val simpleMode: SimpleMode,
    private val taskRepo: TaskRepo,
    private val storageManager: StorageManager,
    private val storageBuilder: StorageBuilder,
    private val taskBuilder: TaskBuilder,
    private val autoSetUp: AutoSetUp,
) : SmartVDC() {

    val navEvents = SingleLiveEvent<NavDirections>()
    val finishEvent = SingleLiveEvent<Unit>()
    val errorEvent = SingleLiveEvent<Throwable>()

    private val editorObs = simpleMode.filesData
        .observeOn(Schedulers.computation())
        .flatMapSingle { data ->
            // Fallback Id doesn't matter, we go to switch case anyways
            taskBuilder.load(data.taskId ?: Task.Id())
                .switchIfEmpty(taskBuilder.createEditor(type = Task.Type.BACKUP_SIMPLE))
        }
        .map { it.editor as SimpleBackupTaskEditor }
        .replayingShare()

    private val editorData = editorObs
        .flatMap { it.editorData }
        .replayingShare()

    private val storageItemObs: Observable<WizardAdapter.Item> = editorData
        .flatMap { storageManager.infos(it.destinations) }
        .map { storageInfos ->
            when (storageInfos.size) {
                0 -> {
                    StorageCreateVH.Item(
                        onSetupStorage = {
                            log(TAG) { "onSetupStorage()" }

                            editorData
                                .take(1)
                                .subscribe { data ->
                                    WizardFilesFragmentDirections.actionWizardFilesFragmentToStoragePicker(
                                        taskId = data.taskId
                                    ).run { navEvents.postValue(this) }
                                }
                        }
                    )
                }
                1 -> {
                    // Show info, allow attach/detach
                    StorageInfoVH.Item(
                        info = storageInfos.single(),
                        onView = {
                            TODO()
                        },
                        onDetach = {
                            TODO()
                        },
                        onDelete = {
                            TODO()
                        }
                    )
                }
                else -> {
                    // Show error? Remove card
                    StorageErrorVH.Item(
                        onView = {
                            TODO()
                        }
                    )
                }
            }
        }

    val state: LiveData<WizardAppsFragmentVDC.State> = Observable
        .combineLatest(editorData, storageItemObs) { editorData, storageItem ->
            val items = mutableListOf<WizardAdapter.Item>()

            if (!editorData.isExistingTask) {
                AutoSetupVH.Item(
                    onAutoSetup = { runAutoSetUp() }
                ).let { items.add(it) }
            }

            items.add(storageItem)

            FilesPathInfoVH.Item(
                sources = emptyList(),
                onAdd = {
                    TODO()
                },
                onRemove = {
                    TODO()
                },
            ).let { items.add(it) }

            WizardAppsFragmentVDC.State(
                items = items,
                isExisting = editorData.isExistingTask
            )
        }
        .doOnError { errorEvent.postValue(it) }
        .onErrorReturnItem(WizardAppsFragmentVDC.State())
        .asLiveData()

    private fun runAutoSetUp() {
        editorData
            .flatMapSingle {
                autoSetUp.setUp(it.taskId, AutoSetUp.Type.FILES)
            }
            .doOnSubscribe {
                // TODO loading mode?
            }
            .doFinally {
                // TODO clear loading mode
            }
            .subscribe({ result ->
                // TODO
            }, {
                // TODO
            })
    }

    fun onStoragePickerResult(result: StoragePickerResult?) {
        log(TAG) { "onStoragePickerResult(result=$result)" }
        if (result == null) return
        editorObs.take(1).subscribe {
            it.addDestination(result.storageId)
        }
    }

    fun onSave() {
        editorObs
            .flatMapSingle { it.save() }
            .subscribe({
                finishEvent.postValue(Unit)
            }, {
                errorEvent.postValue(it)
            })
    }

    data class State(
        val items: List<WizardAdapter.Item> = emptyList(),
        val isExisting: Boolean = false,
    )

    companion object {
        private val TAG = logTag("SimpleMode", "Files", "Wizard", "VDC")
    }
}