package eu.darken.bb.backup.ui.generator.editor.types.files.legacy

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.files.legacy.LegacyFilesSpecGeneratorEditor
import eu.darken.bb.common.Stater
import eu.darken.bb.common.WorkId
import eu.darken.bb.common.addWorkId
import eu.darken.bb.common.clearWorkId
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import io.reactivex.schedulers.Schedulers

class LegacyFilesEditorFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val generatorId: Generator.Id,
        private val builder: GeneratorBuilder
) : SmartVDC(), BaseEditorFragment.VDC {

    private val dataObs = builder.config(generatorId)
            .subscribeOn(Schedulers.io())

    private val editorObs = dataObs
            .filter { it.editor != null }
            .map { it.editor as LegacyFilesSpecGeneratorEditor }

    private val configObs = editorObs.flatMap { it.config }

    private val editor by lazy { editorObs.blockingFirst() }

    private val stater = Stater(State())
    override val state = stater.liveData

    init {
        editorObs.take(1)
                .subscribe { editor ->
                    stater.update {
                        it.copy(
                                workIds = it.clearWorkId(),
                                isExisting = editor.existingConfig
                        )
                    }
                }

        configObs
                .subscribe { config ->
                    stater.update {
                        it.copy(
                                label = config.label,
                                path = config.path.path
                        )
                    }
                }
                .withScopeVDC(this)
    }

    fun updateLabel(label: String) {
        editor.updateLabel(label)
    }

    fun updatePath(path: String) {
        editor.updatePath(path)
    }

    override fun onNavigateBack(): Boolean {
        if (stater.snapshot.isExisting) {
            builder.remove(generatorId)
                    .doOnSubscribe { stater.update { it.copy(workIds = it.addWorkId(WorkId.FOREVER)) } }
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
            val path: String = "",
            val allowCreate: Boolean = false,
            override val isExisting: Boolean = true,
            override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : BaseEditorFragment.VDC.State, WorkId.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<LegacyFilesEditorFragmentVDC> {
        fun create(handle: SavedStateHandle, generatorId: Generator.Id): LegacyFilesEditorFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Generator", "Files", "Editor", "VDC")
    }
}