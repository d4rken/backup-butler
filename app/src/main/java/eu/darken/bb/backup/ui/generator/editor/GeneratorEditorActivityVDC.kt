package eu.darken.bb.backup.ui.generator.editor

import androidx.annotation.IdRes
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class GeneratorEditorActivityVDC @Inject constructor(
    handle: SavedStateHandle,
    private val generatorBuilder: GeneratorBuilder
) : SmartVDC() {

    private val navArgs = handle.navArgs<GeneratorEditorActivityArgs>()
    private val generatorId = navArgs.value.generatorId

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
}