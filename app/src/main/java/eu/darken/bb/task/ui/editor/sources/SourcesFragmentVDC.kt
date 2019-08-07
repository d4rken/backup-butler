package eu.darken.bb.task.ui.editor.sources

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.backup.ui.generator.list.GeneratorConfigOpt
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.task.core.DefaultTask
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import io.reactivex.schedulers.Schedulers

class SourcesFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: Task.Id,
        private val taskBuilder: TaskBuilder,
        private val generatorRepo: GeneratorRepo
) : VDC() {

    private val taskObs = taskBuilder.task(taskId)

    private val sourcesUpdater = taskObs
            .doOnNext { task ->
                val configs = task.sources.map { id ->
                    val config = generatorRepo.get(id).blockingGet().value
                    GeneratorConfigOpt(id, config)
                }
                stateUpdater.update { it.copy(sources = configs) }
            }

    private val stateUpdater: StateUpdater<State> = StateUpdater(State())
            .addLiveDep { sourcesUpdater.subscribe() }

    val state = stateUpdater.state

    val sourcePickerEvent = SingleLiveEvent<List<GeneratorConfigOpt>>()

    data class State(
            val sources: List<GeneratorConfigOpt> = emptyList()
    )

    fun addSource(config: GeneratorConfigOpt) {
        taskBuilder
                .update(taskId) {
                    it as DefaultTask
                    it.copy(
                            sources = it.sources.toMutableSet().apply { add(config.generatorId) }.toSet()
                    )
                }
                .subscribeOn(Schedulers.computation())
                .subscribe()
    }

    fun showSourcePicker() {
        generatorRepo.configs
                .subscribeOn(Schedulers.io())
                .map { it.values }
                .flatMap { all ->
                    taskObs.map { it.sources }.map { alreadyAdded ->
                        return@map all.filter { !alreadyAdded.contains(it.generatorId) }
                    }
                }
                .firstOrError()
                .map { configs -> configs.map { GeneratorConfigOpt(it) } }
                .subscribe { infos ->
                    sourcePickerEvent.postValue(infos.toList())
                }
    }

    fun removeSource(source: GeneratorConfigOpt) {
        taskBuilder
                .update(taskId) { task ->
                    task as DefaultTask
                    task.copy(
                            sources = task.sources
                                    .toMutableSet()
                                    .filterNot { it == source.generatorId }
                                    .toSet()
                    )
                }
                .subscribeOn(Schedulers.computation())
                .subscribe()
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<SourcesFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): SourcesFragmentVDC
    }
}