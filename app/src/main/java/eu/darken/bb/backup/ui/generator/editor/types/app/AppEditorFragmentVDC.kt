package eu.darken.bb.backup.ui.generator.editor.types.app

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.app.AppSpecGeneratorEditor
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import io.reactivex.schedulers.Schedulers

class AppEditorFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val generatorId: Generator.Id,
        private val builder: GeneratorBuilder
) : SmartVDC(), BaseEditorFragment.VDC {

    private val stater = Stater(State())
    override val state = stater.liveData

    private val editorObs = builder.config(generatorId)
            .filter { it.editor != null }
            .map { it.editor as AppSpecGeneratorEditor }

    private val editorDataObs = editorObs.switchMap { it.editorData }

    private val editor: AppSpecGeneratorEditor by lazy { editorObs.blockingFirst() }

    init {
        editorDataObs
                .subscribe { editorData ->
                    stater.update { state ->
                        state.copy(
                                label = editorData.label,
                                isExisting = editorData.isExistingGenerator,
                                isWorking = false
                                // TODO more args
                        )
                    }
                }
                .withScopeVDC(this)
    }

    fun updateLabel(label: String) {
        editor.updateLabel(label)
    }

    fun updateIncludedPackages(pkgs: List<String>) {
        editor.updateIncludedPackages(pkgs)
    }

    override fun onNavigateBack(): Boolean {
        if (stater.snapshot.isExisting) {
            builder.remove(generatorId)
                    .doOnSubscribe { stater.update { it.copy(isWorking = true) } }
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