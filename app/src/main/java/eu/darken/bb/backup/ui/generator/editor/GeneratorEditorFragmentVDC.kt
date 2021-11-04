package eu.darken.bb.backup.ui.generator.editor

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.NavEventsSource
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.via
import eu.darken.bb.common.vdc.SmartVDC
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class GeneratorEditorFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val generatorBuilder: GeneratorBuilder
) : SmartVDC(), NavEventsSource {

    private val navArgs = handle.navArgs<GeneratorEditorFragmentArgs>()
    private val generatorId: Generator.Id = run {
        log(TAG) { "navArgs=${navArgs.value}" }
        val handleKey = "newId"
        when {
            handle.contains(handleKey) -> handle.get<Generator.Id>(handleKey)!!
            navArgs.value.generatorId != null -> navArgs.value.generatorId!!
            else -> Generator.Id().also {
                // ID was null, we create a new one
                handle.set(handleKey, it)
            }
        }
    }

    private val generatorObs = generatorBuilder.load(generatorId)
        .switchIfEmpty(generatorBuilder.getEditor(generatorId))
        .flatMapObservable { generatorBuilder.generator(it.generatorId) }
        .observeOn(Schedulers.computation())

    override val navEvents = SingleLiveEvent<NavDirections>()

    init {
        generatorObs
            .doOnSubscribe {
                log(TAG) { "sub" }
            }
            .map { data ->
                when (data.generatorType) {
                    Backup.Type.APP -> GeneratorEditorFragmentDirections
                        .actionGeneratorEditorFragmentToAppEditorConfigFragment(generatorId = data.generatorId)
                    Backup.Type.FILES -> GeneratorEditorFragmentDirections
                        .actionGeneratorEditorFragmentToFilesEditorFragment(generatorId = data.generatorId)
                    null -> GeneratorEditorFragmentDirections
                        .actionGeneratorEditorFragmentToGeneratorTypeFragment(generatorId = data.generatorId)
                }
            }
            .subscribe { it.via(this) }
    }

    companion object {
        private val TAG = logTag("Generator", "Editor", "VDC")
    }
}