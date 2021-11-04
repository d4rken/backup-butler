package eu.darken.bb.backup.ui.generator.picker

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.backup.ui.generator.list.GeneratorConfigOpt
import eu.darken.bb.backup.ui.generator.list.GeneratorListAdapter
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.NavEventsSource
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.via
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class GeneratorPickerFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    generatorRepo: GeneratorRepo
) : SmartVDC(), NavEventsSource {
    private val navArgs by handle.navArgs<GeneratorPickerFragmentArgs>()
    private val taskId: Task.Id? = navArgs.taskId

    private val editorObs = (taskId?.let { Observable.just(it) } ?: Observable.empty())
        .observeOn(Schedulers.computation())
        .flatMap { taskBuilder.task(it) }
        .filter { it.editor != null }
        .map { it.editor as SimpleBackupTaskEditor }
    private val editorData = editorObs.flatMap { it.editorData }
    override val navEvents = SingleLiveEvent<NavDirections>()

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
                        items = infos.map {
                            GeneratorListAdapter.Item(
                                configOpt = GeneratorConfigOpt(config = it),
                                onClick = {
                                    GeneratorPickerResult(it).run { finishEvent.postValue(this) }
                                }
                            )

                        }.toList(),
                        allExistingAdded = infos.isEmpty() && all.isNotEmpty(),
                        isLoading = false
                    )
                }
        }
        .startWithItem(State())
        .asLiveData()

    val finishEvent = SingleLiveEvent<GeneratorPickerResult>()

    fun createGenerator() {
        GeneratorPickerFragmentDirections.actionGeneratorPickerFragmentToGeneratorEditor(

        ).via(this)
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