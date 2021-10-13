package eu.darken.bb.backup.ui.generator.editor.types

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class GeneratorTypeFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val builder: GeneratorBuilder
) : SmartVDC() {

    private val navArgs = handle.navArgs<GeneratorTypeFragmentArgs>()
    private val generatorId = navArgs.value.generatorId

    private val builderObs = builder.generator(generatorId)

    val navigationEvent = SingleLiveEvent<Pair<Backup.Type, Generator.Id>>()

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
            .flatMapMaybe { builder.load(it.value!!.generatorId) }
            .doFinally { navigationEvent.postValue(type to generatorId) }
            .subscribe()
    }

    data class State(
        val supportedTypes: List<Backup.Type>,
        val isWorking: Boolean = false
    )
}