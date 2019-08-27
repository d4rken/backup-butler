package eu.darken.bb.backup.ui.generator.editor.types.app

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.app.AppSpecGeneratorEditor
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers

class AppEditorFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val generatorId: Generator.Id,
        private val builder: GeneratorBuilder
) : SmartVDC(), BaseEditorFragment.VDC {

    private val stateUpdater = Stater(State())

    private val editorObs = builder.config(generatorId)
            .filter { it.editor != null }
            .map { it.editor as AppSpecGeneratorEditor }

    private val editor: AppSpecGeneratorEditor
        get() = editorObs.blockingFirst()

    private val configObs = editorObs.flatMap { it.config }

    override val state = Observables.combineLatest(stateUpdater.data, configObs)
            .subscribeOn(Schedulers.io())
            .map { (state, config) ->
                state.copy(
                        label = config.label,
                        allowCreate = config.label.isNotEmpty(),
                        includedPackages = config.packagesIncluded.toList()
                )
            }
            .toLiveData()

    init {
        editorObs.firstOrError()
                .subscribeOn(Schedulers.io())
                .subscribe { editor ->
                    stateUpdater.update { it.copy(isWorking = false, isExisting = editor.existingConfig) }
                }
    }

    fun updateLabel(label: String) {
        editor.updateLabel(label)
    }

    fun updateIncludedPackages(pkgs: List<String>) {
        editor.updateIncludedPackages(pkgs)
    }

    override fun onNavigateBack(): Boolean {
        if (stateUpdater.snapshot.isExisting) {
            builder.remove(generatorId)
                    .doOnSubscribe { stateUpdater.update { it.copy(isWorking = true) } }
                    .subscribeOn(Schedulers.io())
                    .subscribe()

        } else {
            builder
                    .update(generatorId) { data ->
                        data!!.copy(generatorType = null, editor = null)
                    }
                    .subscribeOn(Schedulers.io())
                    .subscribe()
        }
        return true
    }

    data class State(
            val label: String = "",
            val allowCreate: Boolean = false,
            val includedPackages: List<String> = emptyList(),
            val isWorking: Boolean = true,
            override val isExisting: Boolean = false
    ) : BaseEditorFragment.VDC.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<AppEditorFragmentVDC> {
        fun create(handle: SavedStateHandle, generatorId: Generator.Id): AppEditorFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Generator", "App", "Editor", "VDC")
    }
}