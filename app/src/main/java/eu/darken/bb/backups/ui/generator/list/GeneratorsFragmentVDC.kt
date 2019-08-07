package eu.darken.bb.backups.ui.generator.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backups.core.GeneratorBuilder
import eu.darken.bb.backups.core.GeneratorRepo
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.rx.toLiveData
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*

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
        generatorBuilder.startEditor().subscribeOn(Schedulers.io()).subscribe()
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
            val generatorId: UUID,
            val allowEdit: Boolean = false,
            val allowDelete: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<GeneratorsFragmentVDC>
}