package eu.darken.bb.backups.ui.generator.editor.types.app

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backups.core.GeneratorBuilder
import eu.darken.bb.backups.core.app.AppSpecGeneratorEditor
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.rx.toLiveData
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import java.util.*

class AppEditorFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val configId: UUID,
        private val builder: GeneratorBuilder
) : SmartVDC() {

    private val stateUpdater = StateUpdater(State())

    private val editorObs = builder.config(configId)
            .filter { it.editor != null }
            .map { it.editor as AppSpecGeneratorEditor }

    private val editor: AppSpecGeneratorEditor
        get() {
            return editorObs.blockingFirst()
        }

    private val configObs = editorObs.flatMap { it.config }

    val state = Observables.combineLatest(stateUpdater.data, configObs)
            .subscribeOn(Schedulers.io())
            .map { (state, config) ->
                state.copy(
                        label = config.label,
                        allowCreate = config.label.isNotEmpty(),
                        includedPackages = config.packagesIncluded.toList()
                )
            }
            .toLiveData()

    val finishActivity = SingleLiveEvent<Boolean>()

    init {
        builder.config(configId)
                .subscribeOn(Schedulers.io())
                .subscribe { data ->
                    stateUpdater.update { it.copy(existing = data.existing) }
                }
        editorObs.firstOrError()
                .subscribeOn(Schedulers.io())
                .subscribe { editor ->
                    stateUpdater.update { it.copy(working = false) }
                }
    }

    fun updateLabel(label: String) {
        editor.updateLabel(label)
    }

    fun updateIncludedPackages(pkgs: List<String>) {
        editor.updateIncludedPackages(pkgs)
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
            val existing: Boolean = false,
            val includedPackages: List<String> = emptyList()
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<AppEditorFragmentVDC> {
        fun create(handle: SavedStateHandle, configId: UUID): AppEditorFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Backup", "Generator", "Editor", "App", "VDC")
    }
}