package eu.darken.bb.main.ui.simple.wizard.files

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.jakewharton.rx3.replayingShare
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.main.core.simple.SimpleMode
import eu.darken.bb.main.ui.simple.wizard.apps.WizardAppsFragmentVDC
import eu.darken.bb.main.ui.simple.wizard.common.*
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.core.StorageRefRepo
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
    private val storageRefRepo: StorageRefRepo,
    private val storageManager: StorageManager,
    private val generatorBuilder: GeneratorBuilder,
    private val taskBuilder: TaskBuilder,
) : SmartVDC() {

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
                            TODO()
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
                    onAutoSetup = {
                        TODO()
                    }
                ).let { items.add(it) }
            }
            items.add(storageItem)

            FilesPathInfoVH.Item(
                sources = emptyList(),
                onAdd = {

                },
                onRemove = {

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

}