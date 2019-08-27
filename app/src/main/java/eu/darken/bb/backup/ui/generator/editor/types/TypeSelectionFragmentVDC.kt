package eu.darken.bb.backup.ui.generator.editor.types

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import io.reactivex.schedulers.Schedulers

class TypeSelectionFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val generatorId: Generator.Id,
        private val builder: GeneratorBuilder
) : SmartVDC(), BaseEditorFragment.VDC {

    override val state = builder.getSupportedBackupTypes()
            .map { types ->
                State(
                        supportedTypes = types.toList()
                )
            }
            .toLiveData()


    fun createType(type: Generator.Type) {
        builder.update(generatorId) { it!!.copy(generatorType = type) }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    override fun onNavigateBack(): Boolean {
        builder.remove(generatorId)
                .subscribeOn(Schedulers.io())
                .subscribe()
        return true
    }

    data class State(
            val supportedTypes: List<Generator.Type>,
            val isWorking: Boolean = false,
            override val isExisting: Boolean = false
    ) : BaseEditorFragment.VDC.State


    @AssistedInject.Factory
    interface Factory : VDCFactory<TypeSelectionFragmentVDC> {
        fun create(handle: SavedStateHandle, generatorId: Generator.Id): TypeSelectionFragmentVDC
    }
}