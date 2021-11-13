package eu.darken.bb.task.ui.editor.backup.generators

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.backup.ui.generator.list.GeneratorConfigOpt
import eu.darken.bb.backup.ui.generator.list.GeneratorListAdapter
import eu.darken.bb.backup.ui.generator.picker.GeneratorPickerResult
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class GeneratorsFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    private val generatorRepo: GeneratorRepo,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {
    private val navArgs by handle.navArgs<GeneratorsFragmentArgs>()
    private val taskId: Task.Id = navArgs.taskId

    private val editorFlow = taskBuilder.task(taskId)
        .filter { it.editor != null }
        .map { it.editor as SimpleBackupTaskEditor }
    private val editorData = editorFlow.flatMapConcat { it.editorData }

    private val stater = DynamicStateFlow(TAG, vdcScope) { State() }
    val state = stater.asLiveData2()

    init {
        editorData
            .onEach { data ->
                val items = data.sources
                    .map { id ->
                        val config = generatorRepo.get(id)
                        GeneratorConfigOpt(id, config)
                    }
                    .map { configOpt ->
                        GeneratorListAdapter.Item(
                            configOpt = configOpt,
                            onClick = { removeSource(it) }
                        )
                    }
                stater.updateBlocking { copy(sources = items) }
            }
            .launchInViewModel()
    }

    private fun removeSource(generatorId: Generator.Id) = launch {
        editorFlow.first().removeGenerator(generatorId)
    }

    fun onAddSource() {
        GeneratorsFragmentDirections.actionSourcesFragmentToGeneratorPicker(
            taskId = taskId
        ).navVia(this)
    }

    fun onNext() {
        GeneratorsFragmentDirections.navActionNext(
            taskId = taskId
        ).navVia(this)
    }

    fun onSourceAdded(result: GeneratorPickerResult?) = launch {
        log(TAG) { "onSourceAdded(result=$result)" }
        if (result == null) return@launch
        editorFlow.first().addGenerator(result.generatorId)
    }

    data class State(
        val sources: List<GeneratorListAdapter.Item> = emptyList()
    )

    companion object {
        val TAG = logTag("Backup", "Editor", "Generators", "List", "VDC")
    }
}