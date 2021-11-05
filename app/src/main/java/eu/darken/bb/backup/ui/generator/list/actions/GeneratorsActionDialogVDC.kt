package eu.darken.bb.backup.ui.generator.list.actions

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.backup.ui.generator.list.actions.GeneratorsAction.DELETE
import eu.darken.bb.backup.ui.generator.list.actions.GeneratorsAction.EDIT
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.navigation.NavEventsSource
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.via
import eu.darken.bb.common.rx.subscribeNullable
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.common.vdc.SmartVDC
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class GeneratorsActionDialogVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val generatorBuilder: GeneratorBuilder,
    private val generatorRepo: GeneratorRepo
) : SmartVDC(), NavEventsSource {

    private val navArgs = handle.navArgs<GeneratorsActionDialogArgs>().value
    private val generatorId: Generator.Id = navArgs.generatorId
    private val stateUpdater = Stater { State(loading = true) }
    val state = stateUpdater.liveData

    override val navEvents = SingleLiveEvent<NavDirections>()
    val closeDialogEvent = SingleLiveEvent<Any>()
    val errorEvent = SingleLiveEvent<Throwable>()

    init {
        generatorRepo.get(generatorId)
            .observeOn(Schedulers.computation())
            .subscribeNullable { config ->
                val actions = listOf(
                    Confirmable(EDIT) { generatorAction(it) },
                    Confirmable(DELETE, requiredLvl = 1) { generatorAction(it) }
                )
                stateUpdater.update {
                    if (config == null) {
                        it.copy(loading = true, finished = true)
                    } else {
                        it.copy(
                            config = config,
                            loading = false,
                            allowedActions = actions
                        )
                    }
                }
            }
    }

    fun generatorAction(action: GeneratorsAction) {
        when (action) {
            EDIT -> {
                generatorBuilder.load(generatorId)
                    .observeOn(Schedulers.computation())
                    .doOnSubscribe { stateUpdater.update { it.copy(loading = true) } }
                    .doFinally { stateUpdater.update { it.copy(loading = false, finished = true) } }
                    .subscribe({
                        GeneratorsActionDialogDirections.actionGeneratorsActionDialogToGeneratorEditor(
                            generatorId = it.generatorId
                        ).via(this)
                        closeDialogEvent.postValue(Any())
                    }, {
                        errorEvent.postValue(it)
                    })
                    .withScopeVDC(this)
            }
            DELETE -> {
                Single.timer(200, TimeUnit.MILLISECONDS)
                    .observeOn(Schedulers.computation())
                    .flatMap { generatorRepo.remove(generatorId) }
                    .doOnSubscribe { stateUpdater.update { it.copy(loading = true) } }
                    .doFinally { stateUpdater.update { it.copy(loading = false, finished = true) } }
                    .subscribe()
            }
        }
    }

    data class State(
        val loading: Boolean = true,
        val finished: Boolean = false,
        val config: Generator.Config? = null,
        val allowedActions: List<Confirmable<GeneratorsAction>> = listOf()
    )
}