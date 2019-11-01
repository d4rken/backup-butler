package eu.darken.bb.backup.ui.generator.editor

import androidx.annotation.IdRes
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import io.reactivex.schedulers.Schedulers


class GeneratorEditorActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val generatorId: Generator.Id,
        private val generatorBuilder: GeneratorBuilder
) : SmartVDC() {

    private val generatorObs = generatorBuilder.generator(generatorId)
            .subscribeOn(Schedulers.io())

    private val editorObs = generatorObs
            .filter { it.editor != null }
            .map { it.editor!! }

    private val stater = Stater {
        val data = generatorObs.blockingFirst()
        val steps = when (data.generatorType) {
            Backup.Type.FILES -> StepFlow.FILES
            Backup.Type.APP -> StepFlow.APP
            else -> StepFlow.SELECTION
        }
        State(stepFlow = steps, generatorId = generatorId, generatorType = data.generatorType)
    }
    val state = stater.liveData

    val finishEvent = SingleLiveEvent<Any>()

    init {
        editorObs
                .flatMap { it.editorData }
                .subscribe { data ->
                    stater.update {
                        it.copy(
                                isExisting = data.isExistingGenerator
                        )
                    }
                }
                .withScopeVDC(this)
    }

    fun dismiss() {
        generatorBuilder.remove(generatorId)
                .subscribeOn(Schedulers.io())
                .subscribe { _ ->
                    finishEvent.postValue(Any())
                }
    }

    data class State(
            val generatorId: Generator.Id,
            val generatorType: Backup.Type?,
            val isExisting: Boolean = false,
            val stepFlow: StepFlow
    )

    enum class StepFlow(@IdRes val start: Int) {
        SELECTION(R.id.generatorTypeFragment),
        FILES(R.id.filesEditorFragment),
        APP(R.id.appEditorConfigFragment);
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<GeneratorEditorActivityVDC> {
        fun create(handle: SavedStateHandle, generatorId: Generator.Id): GeneratorEditorActivityVDC
    }
}