package eu.darken.bb.tasks.ui.editor.sources

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backups.core.GeneratorRepo
import eu.darken.bb.backups.core.SpecGenerator
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.tasks.core.DefaultBackupTask
import eu.darken.bb.tasks.core.TaskBuilder
import io.reactivex.schedulers.Schedulers
import java.util.*

class SourcesFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: UUID,
        private val taskBuilder: TaskBuilder,
        private val generatorRepo: GeneratorRepo
) : VDC() {

    private val taskObs = taskBuilder.task(taskId)
            .doOnNext { task ->
                val sources = task.sources.toList()
                stateUpdater.update { it.copy(sources = sources) }
            }
    private val stateUpdater: StateUpdater<State> = StateUpdater(State())
            .addLiveDep { taskObs.subscribe() }

    val state = stateUpdater.state

    val sourcePickerEvent = SingleLiveEvent<List<SpecGenerator.Config>>()

    data class State(
            val sources: List<SpecGenerator.Config> = emptyList()
    )

    fun addSource(config: SpecGenerator.Config) {
        taskBuilder
                .update(taskId) {
                    it as DefaultBackupTask
                    it.copy(
                            sources = it.sources.toMutableSet().apply { add(config) }.toSet()
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
                        return@map all.filter { !alreadyAdded.contains(it) }
                    }
                }
                .firstOrError()
                .subscribe { infos ->
                    sourcePickerEvent.postValue(infos.toList())
                }
    }

    fun removeSource(source: SpecGenerator.Config) {
        taskBuilder
                .update(taskId) { task ->
                    task as DefaultBackupTask
                    task.copy(
                            sources = task.sources
                                    .toMutableSet()
                                    .filterNot { it.generatorId == source.generatorId }
                                    .toSet()
                    )
                }
                .subscribeOn(Schedulers.computation())
                .subscribe()
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<SourcesFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: UUID): SourcesFragmentVDC
    }
}