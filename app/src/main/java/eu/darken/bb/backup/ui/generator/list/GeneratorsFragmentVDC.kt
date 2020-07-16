package eu.darken.bb.backup.ui.generator.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class GeneratorsFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val generatorRepo: GeneratorRepo,
        private val generatorBuilder: GeneratorBuilder
) : SmartVDC() {

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
            .toLiveData()

    val editTaskEvent = SingleLiveEvent<EditActions>()

    fun newGenerator() {
        generatorBuilder.startEditor()
                .subscribeOn(Schedulers.io())
                .subscribe()
    }


    fun editGenerator(config: GeneratorConfigOpt) {
        Timber.tag(TAG).d("editGenerator(%s)", config)
        editTaskEvent.postValue(EditActions(
                generatorId = config.generatorId,
                allowDelete = true
        ))
    }

    data class ViewState(
            val generators: List<GeneratorConfigOpt>
    )

    data class EditActions(
            val generatorId: Generator.Id,
            val allowEdit: Boolean = false,
            val allowDelete: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<GeneratorsFragmentVDC>

    companion object {
        val TAG = App.logTag("Backup", "GeneratorList", "VDC")
    }
}