package eu.darken.bb.backup.ui.generator.editor.types

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.navigation.NavEventsSource
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.via
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class GeneratorTypeFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val builder: GeneratorBuilder
) : SmartVDC(), NavEventsSource {

    private val navArgs = handle.navArgs<GeneratorTypeFragmentArgs>()
    private val generatorId = navArgs.value.generatorId

    private val builderObs = builder.generator(generatorId)
    override val navEvents = SingleLiveEvent<NavDirections>()
    val state = builder.getSupportedBackupTypes()
        .map { types ->
            State(
                supportedTypes = types.toList()
            )
        }
        .asLiveData()


    fun createType(type: Backup.Type) {
        builder
            .update(generatorId) { it!!.copy(generatorType = type, editor = null) }
            .observeOn(Schedulers.computation())
            .subscribe { data ->
                when (type) {
                    Backup.Type.APP -> GeneratorTypeFragmentDirections
                        .actionGeneratorTypeFragmentToAppEditorFragment(generatorId)
                    Backup.Type.FILES -> GeneratorTypeFragmentDirections
                        .actionGeneratorTypeFragmentToFilesEditorFragment(generatorId)
                }.via(this)
            }
    }

    data class State(
        val supportedTypes: List<Backup.Type>,
        val isWorking: Boolean = false
    )
}