package eu.darken.bb.backup.ui.generator.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.main.ui.MainFragmentDirections
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class GeneratorListFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val generatorRepo: GeneratorRepo,
    dispatcherProvider: DispatcherProvider
) : Smart2VDC(dispatcherProvider) {

    val showSingleUseExplanation = SingleLiveEvent<Unit>()

    val viewState: LiveData<ViewState> = generatorRepo.configs.map { it.values }
        .map { repos ->
            val refs = repos
                .map { config ->
                    GeneratorListAdapter.Item(
                        configOpt = GeneratorConfigOpt(config),
                        onClick = {
                            if (config.isSingleUse) {
                                showSingleUseExplanation.postValue(Unit)
                            } else {
                                editGenerator(it)
                            }
                        }
                    )
                }
            return@map ViewState(generators = refs)
        }
        .asLiveData2()

    fun newGenerator() {
        MainFragmentDirections.actionMainFragmentToGeneratorEditor()
            .navVia(this)
    }

    fun editGenerator(generatorId: Generator.Id) {
        log(TAG) { "editGenerator(generatorId=$generatorId)" }
        MainFragmentDirections.actionMainFragmentToGeneratorsActionDialog(
            generatorId = generatorId,
        ).navVia(this)
    }

    data class ViewState(
        val generators: List<GeneratorListAdapter.Item>
    )

    companion object {
        val TAG = logTag("Generator", "List", "VDC")
    }
}