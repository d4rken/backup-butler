package eu.darken.bb.backup.ui.generator.editor

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.NavEventSource
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class GeneratorEditorFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val generatorBuilder: GeneratorBuilder,
    private val dispatcherProvider: DispatcherProvider
) : Smart2VDC(dispatcherProvider = dispatcherProvider), NavEventSource {

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

    private val generatorFlow: Flow<GeneratorBuilder.Data> = generatorBuilder.generator(generatorId)
        .onStart {
            val data = generatorBuilder.load(generatorId) ?: generatorBuilder.getEditor(generatorId)
            log(TAG) { "Loaded data $data" }
        }

    init {
        generatorFlow
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
            .onEach { it.navVia(this) }
            .launchInViewModel()
    }

    companion object {
        private val TAG = logTag("Generator", "Editor", "VDC")
    }
}