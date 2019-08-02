package eu.darken.bb.backups.ui.editor.types.app

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backups.core.BackupBuilder
import eu.darken.bb.common.HotData
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.rx.toLiveData
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import java.util.*

class AppEditorFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val configId: UUID,
        private val builder: BackupBuilder
) : SmartVDC() {

    private val stateUpdater = HotData(State())

    private val dataObs = builder.config(configId)

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
            builder.remove(configId)
                    .doOnSubscribe { stateUpdater.update { it.copy(working = true) } }
                    .subscribeOn(Schedulers.io())
                    .subscribe { _ ->
                        finishActivity.postValue(true)
                    }
            return true
        } else {
            builder
                    .update(configId) { data ->
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
    interface Factory : VDCFactory<AppEditorFragmentVDC> {
        fun create(handle: SavedStateHandle, configId: UUID): AppEditorFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Backup", "ConfigEditor", "App", "VDC")
    }
}