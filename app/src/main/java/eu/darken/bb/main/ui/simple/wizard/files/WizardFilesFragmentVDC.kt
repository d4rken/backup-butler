package eu.darken.bb.main.ui.simple.wizard.files

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.R
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.main.core.simple.AutoSetUp
import eu.darken.bb.main.core.simple.SimpleModeRepo
import eu.darken.bb.main.ui.simple.wizard.apps.WizardAppsFragmentVDC
import eu.darken.bb.main.ui.simple.wizard.common.*
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.picker.StoragePickerResult
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class WizardFilesFragmentVDC @Inject constructor(
    @ApplicationContext private val context: Context,
    private val handle: SavedStateHandle,
    private val simpleModeRepo: SimpleModeRepo,
    private val storageManager: StorageManager,
    private val taskBuilder: TaskBuilder,
    private val autoSetUp: AutoSetUp,
) : SmartVDC() {

    private val taskIdObs = simpleModeRepo.filesData.data.take(1).singleOrError()
        .observeOn(Schedulers.computation())
        .map { data ->
            val keyId = "keyId"
            (data.taskId // Edit, otherwise new creation
                ?: handle.get<Task.Id>(keyId))
                ?: Task.Id().also { handle.set(keyId, it) }
        }

    val navEvents = SingleLiveEvent<NavDirections?>()
    val errorEvent = SingleLiveEvent<Throwable>()

    private val editorObs: Single<SimpleBackupTaskEditor> = taskIdObs
        .flatMap { taskBuilder.getEditor(taskId = it, type = Task.Type.BACKUP_SIMPLE) }
        .map { it.editor as SimpleBackupTaskEditor }
        .doOnSuccess {
            it.updateLabel(context.getString(R.string.simple_mode_files_default_task_label))
        }
        .cache()

    private val editorData: Observable<SimpleBackupTaskEditor.Data> = editorObs
        .flatMapObservable { it.editorData }

    private val storageItemObs: Observable<WizardAdapter.Item> = editorData
        .switchMap { data ->
            storageManager.infos(data.destinations).takeUntil { infos ->
                infos.all { it.isFinished }
            }
        }
        .map { storageInfos ->
            when (storageInfos.size) {
                0 -> StorageCreateVH.Item(
                    onSetupStorage = {
                        log(TAG) { "onSetupStorage()" }

                        editorData.take(1).subscribe { data ->
                            WizardFilesFragmentDirections.actionWizardFilesFragmentToStoragePicker(
                                taskId = data.taskId
                            ).run { navEvents.postValue(this) }
                        }
                    }
                )
                1 -> StorageInfoVH.Item(
                    infoOpt = storageInfos.single(),
                    onRemove = { storageId ->
                        editorObs.subscribe { editor -> editor.removeStorage(storageId) }
                    },
                )
                else -> StorageErrorMultipleVH.Item

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
        editorObs.subscribe { editor ->
            editor.addStorage(result.storageId)
        }
    }

    fun saveTask() {
        taskIdObs
            .flatMap { taskBuilder.save(it) }
            .flatMap { newTask -> simpleModeRepo.filesData.updateRx { it.copy(taskId = newTask.taskId) } }
            .subscribe({
                navEvents.postValue(null)
            }, {
                errorEvent.postValue(it)
            })
    }

    fun removeTask() {
        log(TAG) { "removeTask()" }
        simpleModeRepo.removeFilesTask().subscribe {
            navEvents.postValue(null)
        }
    }

    data class State(
        val items: List<WizardAdapter.Item> = emptyList(),
        val isExisting: Boolean = false,
    )

    companion object {
        private val TAG = logTag("SimpleMode", "Files", "Wizard", "VDC")
    }
}