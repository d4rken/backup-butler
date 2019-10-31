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

    val finishActivityEvent = SingleLiveEvent<Any>()

    init {
        editorObs
                .flatMap { it.isValid() }
                .subscribe { isValid ->
                    stater.update { it.copy(isValid = isValid) }
                }
                .withScopeVDC(this)

        editorObs
                .flatMap { it.editorData }
                .subscribe { data ->
                    stater.update {
                        it.copy(
                                isExisting = data.isExistingGenerator,
                                isWorking = false
                        )
                    }
                }
                .withScopeVDC(this)
    }

    fun saveConfig() {
        generatorBuilder.save(generatorId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe { stater.update { it.copy(isWorking = true) } }
                .doFinally { finishActivityEvent.postValue(Any()) }
                .subscribe()
    }

    fun dismiss() {
//        TODO("not implemented")
        //         taskBuilder.remove(taskId)
        //                .subscribeOn(Schedulers.io())
        //                .doOnSubscribe {
        //                    stater.update {
        //                        it.copy(isLoading = true)
        //                    }
        //                }
        //                .subscribe { _ ->
        //                    finishEvent.postValue(true)
        //                }
    }

    data class State(
            val generatorId: Generator.Id,
            val generatorType: Backup.Type?,
            val stepFlow: StepFlow,
            @IdRes val currentStep: Int = 0,
            val isExisting: Boolean = false,
            val isValid: Boolean = false,
            val isWorking: Boolean = false
    )

    enum class StepFlow(@IdRes val start: Int) {
        SELECTION(R.id.generatorTypeFragment),
        FILES(R.id.filesEditorFragment),
        APP(R.id.appEditorFragment);
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<GeneratorEditorActivityVDC> {
        fun create(handle: SavedStateHandle, generatorId: Generator.Id): GeneratorEditorActivityVDC
    }
}