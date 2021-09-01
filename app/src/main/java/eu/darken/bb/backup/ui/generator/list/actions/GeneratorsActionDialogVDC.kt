package eu.darken.bb.backup.ui.generator.list.actions

import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.backup.ui.generator.list.actions.GeneratorsAction.DELETE
import eu.darken.bb.backup.ui.generator.list.actions.GeneratorsAction.EDIT
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.subscribeNullable
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class GeneratorsActionDialogVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle,
    @Assisted private val generatorId: Generator.Id,
    private val generatorBuilder: GeneratorBuilder,
    private val generatorRepo: GeneratorRepo
) : SmartVDC() {

    private val stateUpdater = Stater(State(loading = true))
    val state = stateUpdater.liveData

    init {
        generatorRepo.get(generatorId)
            .subscribeOn(Schedulers.io())
            .subscribeNullable { config ->
                val actions = listOf(
                    Confirmable(EDIT),
                    Confirmable(DELETE, requiredLvl = 1)
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
                generatorBuilder.startEditor(generatorId)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { stateUpdater.update { it.copy(loading = true) } }
                    .doFinally { stateUpdater.update { it.copy(loading = false, finished = true) } }
                    .subscribe()
            }
            DELETE -> {
                Single.timer(200, TimeUnit.MILLISECONDS)
                    .flatMap { generatorRepo.remove(generatorId) }
                    .subscribeOn(Schedulers.io())
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

    @AssistedFactory
    interface Factory : VDCFactory<GeneratorsActionDialogVDC> {
        fun create(handle: SavedStateHandle, generatorId: Generator.Id): GeneratorsActionDialogVDC
    }
}