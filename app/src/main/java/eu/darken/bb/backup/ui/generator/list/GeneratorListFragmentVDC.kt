package eu.darken.bb.backup.ui.generator.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.NavEventsSource
import eu.darken.bb.common.navigation.via
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.main.ui.MainFragmentDirections
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GeneratorListFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val generatorRepo: GeneratorRepo,
) : SmartVDC(), NavEventsSource {

    val viewState: LiveData<ViewState> = generatorRepo.configs.map { it.values }
        .map { repos ->
            val refs = repos
                .map { config ->
                    GeneratorListAdapter.Item(
                        configOpt = GeneratorConfigOpt(config),
                        onClick = { editGenerator(it) }
                    )
                }
            return@map ViewState(generators = refs)
        }
        .asLiveData()

    override val navEvents = SingleLiveEvent<NavDirections>()

    fun newGenerator() {
        MainFragmentDirections.actionMainFragmentToGeneratorEditor()
            .via(this)
    }


    fun editGenerator(generatorId: Generator.Id) {
        Timber.tag(TAG).d("editGenerator(%s)", generatorId)
        MainFragmentDirections.actionMainFragmentToGeneratorsActionDialog(
            generatorId = generatorId,
        ).via(this)
    }

    data class ViewState(
        val generators: List<GeneratorListAdapter.Item>
    )

    companion object {
        val TAG = logTag("Generator", "List", "VDC")
    }
}