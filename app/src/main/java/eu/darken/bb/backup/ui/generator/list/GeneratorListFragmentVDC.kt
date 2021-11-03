package eu.darken.bb.backup.ui.generator.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.NavEventsSource
import eu.darken.bb.common.navigation.via
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.main.ui.MainFragmentDirections
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GeneratorListFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val generatorRepo: GeneratorRepo,
    private val generatorBuilder: GeneratorBuilder
) : SmartVDC(), NavEventsSource {

    val viewState: LiveData<ViewState> = generatorRepo.configs.map { it.values }
        .map { repos ->
            val refs = repos.map { GeneratorConfigOpt(it) }
            return@map ViewState(
                generators = refs
            )
        }
        .doOnSubscribe {
            Timber.i("TestSub")
        }
        .doFinally {
            Timber.i("Finally")
        }
        .asLiveData()

    override val navEvents = SingleLiveEvent<NavDirections>()

    fun newGenerator() {
        generatorBuilder.getEditor()
            .observeOn(Schedulers.computation())
            .subscribe { id ->
                MainFragmentDirections.actionMainFragmentToGeneratorEditorActivity(
                    generatorId = id
                ).via(this)
            }
    }


    fun editGenerator(config: GeneratorConfigOpt) {
        Timber.tag(TAG).d("editGenerator(%s)", config)
        MainFragmentDirections.actionMainFragmentToGeneratorsActionDialog(
            generatorId = config.generatorId,
        ).via(this)
    }

    data class ViewState(
        val generators: List<GeneratorConfigOpt>
    )

    companion object {
        val TAG = logTag("Backup", "GeneratorList", "VDC")
    }
}