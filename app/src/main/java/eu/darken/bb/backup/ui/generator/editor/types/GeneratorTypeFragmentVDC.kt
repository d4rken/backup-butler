package eu.darken.bb.backup.ui.generator.editor.types

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import io.reactivex.schedulers.Schedulers

class GeneratorTypeFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val generatorId: Generator.Id,
        private val builder: GeneratorBuilder
) : SmartVDC() {

    private val builderObs = builder.generator(generatorId)

    val navigationEvent = SingleLiveEvent<Pair<Backup.Type, Generator.Id>>()

    val state = builder.getSupportedBackupTypes()
            .map { types ->
                State(
                        supportedTypes = types.toList()
                )
            }
            .toLiveData()


    fun createType(type: Backup.Type) {
        builder.update(generatorId) { it!!.copy(generatorType = type) }
                .subscribeOn(Schedulers.io())
                .doFinally { navigationEvent.postValue(type to generatorId) }
                .subscribe()
    }

    data class State(
            val supportedTypes: List<Backup.Type>,
            val isWorking: Boolean = false
    )


    @AssistedInject.Factory
    interface Factory : VDCFactory<GeneratorTypeFragmentVDC> {
        fun create(handle: SavedStateHandle, generatorId: Generator.Id): GeneratorTypeFragmentVDC
    }
}