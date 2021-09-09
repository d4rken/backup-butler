package eu.darken.bb.task.ui.editor.backup.sources.picker

import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.App
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.backup.ui.generator.list.GeneratorConfigOpt
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.rx.latest
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class GeneratorPickerFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    @Assisted private val taskId: Task.Id,
    private val taskBuilder: TaskBuilder,
    private val generatorBuilder: GeneratorBuilder,
    generatorRepo: GeneratorRepo
) : SmartVDC() {

    private val editorObs = taskBuilder.task(taskId)
        .subscribeOn(Schedulers.io())
        .filter { it.editor != null }
        .map { it.editor as SimpleBackupTaskEditor }
    private val editorData = editorObs.flatMap { it.editorData }

    val generatorData = generatorRepo.configs
        .subscribeOn(Schedulers.io())
        .map { it.values }
        .flatMap { all ->
            editorData
                .map { it.sources }.map { alreadyAdded ->
                    return@map all.filter { !alreadyAdded.contains(it.generatorId) }
                }
                .map { infos ->
                    State(
                        generatorData = infos.map { GeneratorConfigOpt(config = it) }.toList(),
                        allExistingAdded = infos.isEmpty() && all.isNotEmpty(),
                        isLoading = false
                    )
                }
        }
        .startWithItem(State())
        .toLiveData()

    val finishEvent = SingleLiveEvent<Any>()

    fun createGenerator() {
        generatorBuilder.startEditor()
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun selectGenerator(item: GeneratorConfigOpt) {
        taskBuilder.task(taskId).subscribeOn(Schedulers.io())
            .latest()
            .map { it.editor as SimpleBackupTaskEditor }
            .subscribe { editor ->
                editor.addSource(item.generatorId)
            }
        finishEvent.postValue(Any())
    }

    data class State(
        val generatorData: List<GeneratorConfigOpt> = emptyList(),
        val allExistingAdded: Boolean = false,
        val isLoading: Boolean = true
    )

    companion object {
        val TAG = App.logTag("Task", "Editor", "Sources", "Picker", "VDC")
    }
}