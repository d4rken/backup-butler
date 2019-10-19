package eu.darken.bb.task.ui.editor.backup.sources

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.backup.ui.generator.list.GeneratorConfigOpt
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import io.reactivex.schedulers.Schedulers

class SourcesFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: Task.Id,
        private val taskBuilder: TaskBuilder,
        private val generatorRepo: GeneratorRepo
) : SmartVDC() {
    private val editorObs = taskBuilder.task(taskId)
            .filter { it.editor != null }
            .map { it.editor as SimpleBackupTaskEditor }
    private val editor: SimpleBackupTaskEditor by lazy { editorObs.blockingFirst() }

    private val editorData = editorObs.flatMap { it.editorData }

    private val stater: Stater<State> = Stater(State())
    val state = stater.liveData

    val sourcePickerEvent = SingleLiveEvent<List<GeneratorConfigOpt>>()

    init {
        editorData
                .subscribe { data ->
                    val configs = data.sources.map { id ->
                        val config = generatorRepo.get(id).blockingGet().value
                        GeneratorConfigOpt(id, config)
                    }
                    stater.update { it.copy(sources = configs) }
                }
                .withScopeVDC(this)
    }

    data class State(
            val sources: List<GeneratorConfigOpt> = emptyList()
    )

    fun addSource(config: GeneratorConfigOpt) {
        editor.addSource(config.generatorId)
    }

    fun removeSource(source: GeneratorConfigOpt) {
        editor.removeSource(source.generatorId)
    }

    fun showSourcePicker() {
        generatorRepo.configs
                .subscribeOn(Schedulers.io())
                .map { it.values }
                .flatMap { all ->
                    editorData.map { it.sources }.map { alreadyAdded ->
                        return@map all.filter { !alreadyAdded.contains(it.generatorId) }
                    }
                }
                .firstOrError()
                .map { configs -> configs.map { GeneratorConfigOpt(it) } }
                .subscribe { infos ->
                    sourcePickerEvent.postValue(infos.toList())
                }
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<SourcesFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): SourcesFragmentVDC
    }
}