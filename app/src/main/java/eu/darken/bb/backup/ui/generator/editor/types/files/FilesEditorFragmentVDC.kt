package eu.darken.bb.backup.ui.generator.editor.types.files

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.files.FilesSpecGeneratorEditor
import eu.darken.bb.common.*
import eu.darken.bb.common.file.SAFPath
import eu.darken.bb.common.file.SimplePath
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.ui.BaseEditorFragment
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.saf.SAFGateway
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class FilesEditorFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val generatorId: Generator.Id,
        private val builder: GeneratorBuilder,
        private val safGateway: SAFGateway
) : SmartVDC(), BaseEditorFragment.VDC {

    private val dataObs = builder.config(generatorId)
            .subscribeOn(Schedulers.io())

    private val editorObs = dataObs
            .filter { it.editor != null }
            .map { it.editor as FilesSpecGeneratorEditor }

    private val configObs = editorObs.flatMap { it.config }

    private val editor by lazy { editorObs.blockingFirst() }

    private val stater = Stater(State())
    override val state = stater.liveData

    val openSAFPickerEvent = SingleLiveEvent<Intent>()

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

    fun updatePathSAF(uri: Uri) {
        Timber.tag(TAG).d("updatePathSAF(uri=%s)", uri)
        val path = SAFPath.build(uri)
        editor.updatePath(path)
    }

    fun updatePathRoot(path: String) {
        val rootPath = SimplePath.build(path)
        editor.updatePath(rootPath)
        TODO("Need to test root here")
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

    fun showPicker() {
        openSAFPickerEvent.postValue(safGateway.createPickerIntent())
    }

    data class State(
            val label: String = "",
            val path: String = "",
            val pathError: Exception? = null,
            override val isExisting: Boolean = true,
            override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : BaseEditorFragment.VDC.State, WorkId.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<FilesEditorFragmentVDC> {
        fun create(handle: SavedStateHandle, generatorId: Generator.Id): FilesEditorFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Generator", "Files", "Editor", "VDC")
    }
}