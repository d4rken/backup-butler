package eu.darken.bb.backup.ui.generator.editor.types.app.preview

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.app.AppSpecGeneratorEditor
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory

class AppEditorPreviewFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val generatorId: Generator.Id,
        @Assisted private val previewMode: PreviewMode,
        private val builder: GeneratorBuilder
) : SmartVDC() {

    private val stater = Stater(State())
    val state = stater.liveData

    private val editorObs = builder.generator(generatorId)
            .filter { it.editor != null }
            .map { it.editor as AppSpecGeneratorEditor }

    private val editorDataObs = editorObs.switchMap { it.editorData }

    private val editor: AppSpecGeneratorEditor by lazy { editorObs.blockingFirst() }

    init {
        editorDataObs
                .subscribe { editorData ->
                    stater.update { state ->
                        state.copy(
                                label = editorData.label
                        )
                    }
                }
                .withScopeVDC(this)
    }

    fun updateLabel(label: String) {
        editor.updateLabel(label)
    }


    data class State(
            val label: String = ""
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<AppEditorPreviewFragmentVDC> {
        fun create(handle: SavedStateHandle, generatorId: Generator.Id, previewMode: PreviewMode): AppEditorPreviewFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Generator", "App", "Editor", "Preview", "VDC")
    }
}