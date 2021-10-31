package eu.darken.bb.task.ui.editor.backup.sources.picker

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.backup.ui.generator.list.GeneratorConfigOpt
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.rx.latest
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class GeneratorPickerFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    private val generatorBuilder: GeneratorBuilder,
    generatorRepo: GeneratorRepo
) : SmartVDC() {
    private val navArgs by handle.navArgs<GeneratorPickerFragmentArgs>()
    private val taskId: Task.Id = navArgs.taskId

    private val editorObs = taskBuilder.task(taskId)
        .observeOn(Schedulers.computation())
        .filter { it.editor != null }
        .map { it.editor as SimpleBackupTaskEditor }
    private val editorData = editorObs.flatMap { it.editorData }

    val generatorData = generatorRepo.configs
        .observeOn(Schedulers.computation())
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
        .asLiveData()

    val finishEvent = SingleLiveEvent<Any>()

    fun createGenerator() {
        generatorBuilder.getEditor()
            .observeOn(Schedulers.computation())
            .doOnSuccess { generatorBuilder.launchEditor(it) }
            .subscribe()
    }

    fun selectGenerator(item: GeneratorConfigOpt) {
        taskBuilder.task(taskId)
            .observeOn(Schedulers.computation())
            .latest()
            .map { it.editor as SimpleBackupTaskEditor }
            .subscribe { editor ->
                editor.addGenerator(item.generatorId)
            }
        finishEvent.postValue(Any())
    }

    data class State(
        val generatorData: List<GeneratorConfigOpt> = emptyList(),
        val allExistingAdded: Boolean = false,
        val isLoading: Boolean = true
    )

    companion object {
        val TAG = logTag("Task", "Editor", "Sources", "Picker", "VDC")
    }
}