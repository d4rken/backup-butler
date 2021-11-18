package eu.darken.bb.backup.ui.generator.list.actions

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.backup.ui.generator.list.actions.GeneratorsAction.DELETE
import eu.darken.bb.backup.ui.generator.list.actions.GeneratorsAction.EDIT
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.Logging.Priority.WARN
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.common.ui.Confirmable
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class GeneratorsActionDialogVDC @Inject constructor(
    handle: SavedStateHandle,
    private val generatorBuilder: GeneratorBuilder,
    private val generatorRepo: GeneratorRepo,
    dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private val navArgs = handle.navArgs<GeneratorsActionDialogArgs>().value
    private val generatorId: Generator.Id = navArgs.generatorId
    private val stater = DynamicStateFlow(TAG, vdcScope) { State(loading = true) }
    val state = stater.asLiveData2()

    val closeDialogEvent = SingleLiveEvent<Any>()

    init {
        launch {
            val config = generatorRepo.get(generatorId)
            val actions = listOf(
                Confirmable(EDIT) { generatorAction(it) },
                Confirmable(DELETE, requiredLvl = 1) { generatorAction(it) }
            )
            stater.updateBlocking {
                if (config == null) {
                    copy(loading = true, finished = true)
                } else {
                    copy(
                        config = config,
                        loading = false,
                        allowedActions = actions
                    )
                }
            }
        }
    }

    fun generatorAction(action: GeneratorsAction) = launch {
        stater.updateBlocking { copy(loading = true) }
        try {
            when (action) {
                EDIT -> {
                    val data = generatorBuilder.load(generatorId)
                    if (data == null) {
                        log(TAG, WARN) { "Couldn't load data for $generatorId" }
                        return@launch
                    }
                    GeneratorsActionDialogDirections.actionGeneratorsActionDialogToGeneratorEditor(
                        generatorId = data.generatorId
                    ).navVia(this@GeneratorsActionDialogVDC)

                    closeDialogEvent.postValue(Any())
                }
                DELETE -> {
                    delay(200)
                    generatorRepo.remove(generatorId)
                }
            }
        } finally {
            stater.updateBlocking { copy(loading = false, finished = true) }
        }
    }

    data class State(
        val loading: Boolean = true,
        val finished: Boolean = false,
        val config: Generator.Config? = null,
        val allowedActions: List<Confirmable<GeneratorsAction>> = listOf()
    )

    companion object {
        val TAG = logTag("Generator", "ActionDialog", "VDC")
    }
}