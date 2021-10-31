package eu.darken.bb.quickmode.ui.files.config

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.backup.core.files.FilesSpecGeneratorEditor
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.ui.picker.PathPicker
import eu.darken.bb.common.navigation.NavEventsSource
import eu.darken.bb.common.navigation.via
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.quickmode.core.AutoSetUp
import eu.darken.bb.quickmode.core.QuickMode
import eu.darken.bb.quickmode.core.QuickModeRepo
import eu.darken.bb.quickmode.ui.apps.config.AppsConfigFragmentVDC
import eu.darken.bb.quickmode.ui.common.config.*
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
class FilesConfigFragmentVDC @Inject constructor(
    @ApplicationContext private val context: Context,
    private val handle: SavedStateHandle,
    private val quickModeRepo: QuickModeRepo,
    private val storageManager: StorageManager,
    private val taskBuilder: TaskBuilder,
    private val autoSetUp: AutoSetUp,
    private val generatorBuilder: GeneratorBuilder,
    private val generatorRepo: GeneratorRepo,
) : SmartVDC(), NavEventsSource {

    private val taskIdObs = quickModeRepo.filesData.data.take(1).singleOrError()
        .observeOn(Schedulers.computation())
        .map { data ->
            val keyId = "keyId"
            (data.taskId // Edit, otherwise new creation
                ?: handle.get<Task.Id>(keyId))
                ?: Task.Id().also { handle.set(keyId, it) }
        }

    override val navEvents = SingleLiveEvent<NavDirections?>()
    val errorEvent = SingleLiveEvent<Throwable>()
    val pathPickerEvent = SingleLiveEvent<PathPicker.Options>()

    private val editorObs: Single<SimpleBackupTaskEditor> = taskIdObs
        .flatMap { taskBuilder.getEditor(taskId = it, type = Task.Type.BACKUP_SIMPLE) }
        .map { it.editor as SimpleBackupTaskEditor }
        .doOnSuccess {
            it.updateLabel(context.getString(R.string.quick_files_default_task_label))
        }
        .cache()

    private val editorData: Observable<SimpleBackupTaskEditor.Data> = editorObs
        .flatMapObservable { it.editorData }

    private val storageItemObs: Observable<ConfigAdapter.Item> = editorData
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
                            FilesConfigFragmentDirections.actionFilesConfigFragmentToStoragePicker(
                                taskId = data.taskId
                            ).via(this)
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

    private val sourcesObs = editorData
        .flatMapSingle { data ->
            Observable.just(data.sources)
                .flatMapIterable { it }
                .flatMapMaybe { generatorRepo.get(it) }
                .toList()
        }


    val state: LiveData<AppsConfigFragmentVDC.State> = Observable
        .combineLatest(editorData, storageItemObs, sourcesObs) { editorData, storageItem, sources ->
            val items = mutableListOf<ConfigAdapter.Item>()

            if (!editorData.isExistingTask) {
                AutoSetupVH.Item(
                    onAutoSetup = { runAutoSetUp() }
                ).run { items.add(this) }
            }

            FilesOptionVH.Item(
                replaceExisting = false,
                replaceExistingOnToggle = {

                },
            ).run { items.add(this) }


            AppsConfigFragmentVDC.State(
                items = items,
                isExisting = editorData.isExistingTask
            )
        }
        .doOnError { errorEvent.postValue(it) }
        .onErrorReturnItem(AppsConfigFragmentVDC.State())
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
            .flatMap { newTask -> quickModeRepo.filesData.updateRx { it.copy(taskId = newTask.taskId) } }
            .subscribe({
                navEvents.postValue(null)
            }, {
                errorEvent.postValue(it)
            })
    }

    fun removeTask() {
        log(TAG) { "removeTask()" }
        quickModeRepo.removeTask(QuickMode.Type.FILES).subscribe {
            navEvents.postValue(null)
        }
    }

    fun onPathPickerResult(result: PathPicker.Result?) {
        log(TAG) { "onPathPickerResult(result=$result)" }
        if (result == null || !result.isSuccess) return
        taskIdObs
            .flatMap { generatorBuilder.getEditor(type = Backup.Type.FILES) }
            .flatMap { generatorBuilder.generator(it).firstOrError() }
            .map { it.editor as FilesSpecGeneratorEditor }
            .flatMap { generatorEditor ->
                generatorEditor
                    .updatePath(result.selection!!.first())
                    .andThen(generatorBuilder.save(generatorEditor.generatorId))
            }
            .flatMap { generatorConfig -> editorObs.map { generatorConfig to it } }
            .subscribe({ (config, editor) ->
                log(TAG) { "Path picker result saved $config / $editor" }
                editor.addGenerator(config.generatorId)
            }, {
                errorEvent.postValue(it)
            })
    }

    data class State(
        val items: List<ConfigAdapter.Item> = emptyList(),
        val isExisting: Boolean = false,
    )

    companion object {
        private val TAG = logTag("QuickMode", "Files", "Wizard", "VDC")
    }
}