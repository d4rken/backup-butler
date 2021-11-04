package eu.darken.bb.task.ui.editor.backup.sources

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.backup.ui.generator.list.GeneratorConfigOpt
import eu.darken.bb.backup.ui.generator.list.GeneratorListAdapter
import eu.darken.bb.backup.ui.generator.picker.GeneratorPickerResult
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.NavEventsSource
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.via
import eu.darken.bb.common.rx.blockingGet2
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import javax.inject.Inject

@HiltViewModel
class SourcesFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    private val generatorRepo: GeneratorRepo
) : SmartVDC(), NavEventsSource {
    private val navArgs by handle.navArgs<SourcesFragmentArgs>()
    private val taskId: Task.Id = navArgs.taskId

    private val editorObs = taskBuilder.task(taskId)
        .filter { it.editor != null }
        .map { it.editor as SimpleBackupTaskEditor }
    private val editorData = editorObs.flatMap { it.editorData }

    private val editor: SimpleBackupTaskEditor by lazy { editorObs.blockingFirst() }

    private val stater: Stater<State> = Stater { State() }
    val state = stater.liveData

    override val navEvents = SingleLiveEvent<NavDirections>()

    init {
        editorData
            .subscribe { data ->
                val items = data.sources
                    .map { id ->
                        val config = generatorRepo.get(id).blockingGet2()
                        GeneratorConfigOpt(id, config)
                    }
                    .map { configOpt ->
                        GeneratorListAdapter.Item(
                            configOpt = configOpt,
                            onClick = { removeSource(it) }
                        )
                    }
                stater.update { it.copy(sources = items) }
            }
            .withScopeVDC(this)
    }

    private fun removeSource(generatorId: Generator.Id) {
        editor.removeGenerator(generatorId)
    }


    fun onAddSource() {
        SourcesFragmentDirections.actionSourcesFragmentToGeneratorPicker(
            taskId = taskId
        ).via(this)
    }

    fun onNext() {
        SourcesFragmentDirections.navActionNext(
            taskId = taskId
        ).via(this)
    }

    fun onSourceAdded(result: GeneratorPickerResult?) {
        log(TAG) { "onSourceAdded(result=$result)" }
        if (result == null) return
        editor.addGenerator(result.generatorId)
    }

    data class State(
        val sources: List<GeneratorListAdapter.Item> = emptyList()
    )

    companion object {
        val TAG = logTag("Backup", "Editor", "Sources", "List", "VDC")
    }
}