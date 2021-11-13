package eu.darken.bb.backup.ui.generator.picker

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.backup.ui.generator.list.GeneratorConfigOpt
import eu.darken.bb.backup.ui.generator.list.GeneratorListAdapter
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class GeneratorPickerFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    generatorRepo: GeneratorRepo,
    dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {
    private val navArgs by handle.navArgs<GeneratorPickerFragmentArgs>()
    private val taskId: Task.Id? = navArgs.taskId

    private suspend fun getAlreadyAdded(): Set<Generator.Id> {
        if (taskId == null) return emptySet()

        val data = taskBuilder.task(taskId).first()
        if (data.editor == null) return emptySet()

        data.editor as SimpleBackupTaskEditor

        return data.editor.editorData.first().sources
    }

    val generatorData = generatorRepo.configs
        .map { it.values }
        .map { all ->
            val alreadyAdded = getAlreadyAdded()
            val available = all.filter { !alreadyAdded.contains(it.generatorId) }
            State(
                items = available.map { config ->
                    GeneratorListAdapter.Item(
                        configOpt = GeneratorConfigOpt(config = config),
                        onClick = {
                            GeneratorPickerResult(it).run { finishEvent.postValue(this) }
                        }
                    )

                }.toList(),
                allExistingAdded = available.isEmpty() && all.isNotEmpty(),
                isLoading = false
            )
        }
        .onStart { emit(State()) }
        .asLiveData2()

    val finishEvent = SingleLiveEvent<GeneratorPickerResult>()

    fun createGenerator() {
        GeneratorPickerFragmentDirections.actionGeneratorPickerFragmentToGeneratorEditor(

        ).navVia(this)
    }

    fun selectGenerator(item: GeneratorConfigOpt) {

    }

    data class State(
        val items: List<GeneratorListAdapter.Item> = emptyList(),
        val allExistingAdded: Boolean = false,
        val isLoading: Boolean = true
    )

    companion object {
        val TAG = logTag("Generator", "Picker", "VDC")
    }
}