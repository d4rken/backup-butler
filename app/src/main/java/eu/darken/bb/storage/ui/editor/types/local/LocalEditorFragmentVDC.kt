package eu.darken.bb.storage.ui.editor.types.local

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.HotData
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.file.JavaFile
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.local.LocalStorageConfigEditor
import eu.darken.bb.storage.core.local.LocalStorageStorageRef
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*

class LocalEditorFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: UUID,
        @AppContext private val context: Context,
        private val storageBuilder: StorageBuilder,
        private val editor: LocalStorageConfigEditor
) : SmartVDC() {

    private val stateUpdater = HotData(State())
    val state = Observables
            .combineLatest(
                    stateUpdater.data,
                    storageBuilder.storage(storageId)
            )
            .map { (state, builder) ->
                val config = builder.config as LocalStorageConfigEditor
                state.copy(
                        canGoBack = builder.ref == null
                )
            }
            .toLiveData()

    init {
        // If not null, get the path put it into the state and prevent the path edittext from being edited,
        // Can't change existing paths for now
        TODO()

        // If the ref is not null then try to load an existing config


        // LocalStorageConfigEditor should be used to load/save the data to have a single location that deals with different config revisions

        storageBuilder.storage(storageId)
                .subscribeOn(Schedulers.io())
                .firstOrError()
                .filter { it.ref != null }
                .map { it.ref!! }
                .flatMapSingle { editor.load(it) }
                .subscribe { config ->
                    configUpdater.update { config }
                }


    }

    fun createStorage() {
        // Create a new ref with path
        // Save the config to the ref
        // Save the ref into the repo -> other tools pick it up

        stateUpdater.data
                .subscribeOn(Schedulers.io())
                .firstOrError()
                .map { state ->
                    LocalStorageStorageRef(
                            storageId = storageId,
                            path = JavaFile.build(state.path)
                    )
                }
                .flatMap { ref ->
                    editor.save(ref).map { ref }
                }
                .flatMap {
                    storageBuilder.save(storageId)
                }
                .subscribe()

        TODO()
    }

    fun updatePath(text: CharSequence) {
        // Where do we update the config???
        // Update config in the state? Or as Hotdata
        TODO()
        stateUpdater.update {
            it.copy(
                    path = text.toString(),
                    validPath = isValidPath(text.toString())
            )
        }
    }

    private fun isValidPath(path: String): Boolean = try {
        var file = File(path)
        while (!file.exists() && file.parent != null) {
            file = file.parentFile
        }
        file.isDirectory && file.canRead() && file.canWrite() && file.canExecute()
    } catch (e: Exception) {
        false
    }

    fun onGoBack(): Boolean {
        storageBuilder
                .update(storageId) { data ->
                    data!!.copy(
                            storageType = null
                    )
                }
                .subscribeOn(Schedulers.io())
                .subscribe()
        return true
    }

    data class State(
            val path: String = "",
            val validPath: Boolean = false,
            val canGoBack: Boolean = false,
            val working: Boolean = true
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<LocalEditorFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: UUID): LocalEditorFragmentVDC
    }
}