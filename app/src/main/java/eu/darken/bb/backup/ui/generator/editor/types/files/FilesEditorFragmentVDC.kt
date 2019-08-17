package eu.darken.bb.backup.ui.generator.editor.types.files

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.common.HotData
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers

class FilesEditorFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val generatorId: Generator.Id,
        private val builder: GeneratorBuilder
) : SmartVDC() {

    private val stateUpdater = HotData(State())

    private val dataObs = builder.config(generatorId)

    private val editorObs = dataObs
            .filter { it.editor != null }
            .map { it.editor!! }

    private val data by lazy { dataObs.firstOrError().blockingGet() }

    val state = Observables.combineLatest(stateUpdater.data, editorObs)
            .subscribeOn(Schedulers.io())
            .map { (state, editor) ->
                state.copy(

                        working = false
                )
            }
            .toLiveData()


    val finishActivity = SingleLiveEvent<Boolean>()

    fun createConfig() {

    }

    fun onGoBack(): Boolean {
        if (stateUpdater.snapshot.existing) {
            builder.remove(generatorId)
                    .doOnSubscribe { stateUpdater.update { it.copy(working = true) } }
                    .subscribeOn(Schedulers.io())
                    .subscribe { _ ->
                        finishActivity.postValue(true)
                    }
            return true
        } else {
            builder
                    .update(generatorId) { data ->
                        data!!.copy(type = null, editor = null)
                    }
                    .subscribeOn(Schedulers.io())
                    .subscribe()
            return true
        }
    }

    data class State(
            val label: String = "",
            val working: Boolean = true,
            val allowCreate: Boolean = false,
            val existing: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<FilesEditorFragmentVDC> {
        fun create(handle: SavedStateHandle, generatorId: Generator.Id): FilesEditorFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Generator", "Editor", "Files", "VDC")
    }
}