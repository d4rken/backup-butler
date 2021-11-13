package eu.darken.bb.backup.ui.generator.editor.types

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class GeneratorTypeFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val builder: GeneratorBuilder,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private val navArgs = handle.navArgs<GeneratorTypeFragmentArgs>()
    private val generatorId = navArgs.value.generatorId

    private val builderFlow = builder.generator(generatorId)
    val state = builder.getSupportedBackupTypes()
        .map { types ->
            State(
                supportedTypes = types.toList()
            )
        }
        .asLiveData2()


    fun createType(type: Backup.Type) = launch {
        val update = builder.update(generatorId) { it!!.copy(generatorType = type, editor = null) }
        when (type) {
            Backup.Type.APP -> GeneratorTypeFragmentDirections
                .actionGeneratorTypeFragmentToAppEditorFragment(generatorId)
            Backup.Type.FILES -> GeneratorTypeFragmentDirections
                .actionGeneratorTypeFragmentToFilesEditorFragment(generatorId)
        }.navVia(this@GeneratorTypeFragmentVDC)
    }

    data class State(
        val supportedTypes: List<Backup.Type>,
        val isWorking: Boolean = false
    )
}