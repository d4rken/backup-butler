package eu.darken.bb.backups.ui.generator.list.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backups.core.GeneratorBuilder
import eu.darken.bb.backups.core.GeneratorRepo
import eu.darken.bb.backups.ui.generator.list.actions.GeneratorsAction.*
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.VDCFactory
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class GeneratorsActionDialogVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val generatorId: UUID,
        private val generatorBuilder: GeneratorBuilder,
        private val generatorRepo: GeneratorRepo
) : SmartVDC() {

    private val stateUpdater = StateUpdater(State(loading = true))
    val state = stateUpdater.state

    init {
        generatorRepo.get(generatorId)
                .subscribeOn(Schedulers.io())
                .subscribe { maybeTask ->
                    val config = maybeTask.value
                    stateUpdater.update {
                        if (config == null) {
                            it.copy(loading = true, finished = true)
                        } else {
                            it.copy(
                                    taskName = config.label,
                                    loading = false,
                                    allowedActions = values().toList()
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
                        .delay(200, TimeUnit.MILLISECONDS)
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
            val loading: Boolean = false,
            val finished: Boolean = false,
            val taskName: String = "",
            val allowedActions: List<GeneratorsAction> = listOf()
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<GeneratorsActionDialogVDC> {
        fun create(handle: SavedStateHandle, generatorId: UUID): GeneratorsActionDialogVDC
    }
}