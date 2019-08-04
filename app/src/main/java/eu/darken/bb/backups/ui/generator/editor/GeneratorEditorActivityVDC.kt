package eu.darken.bb.backups.ui.generator.editor

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backups.core.Backup
import eu.darken.bb.backups.core.GeneratorBuilder
import eu.darken.bb.backups.ui.generator.editor.types.TypeSelectionFragment
import eu.darken.bb.backups.ui.generator.editor.types.app.AppEditorFragment
import eu.darken.bb.backups.ui.generator.editor.types.files.FilesEditorFragment
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.rx.toLiveData
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.reflect.KClass


class GeneratorEditorActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val configId: UUID,
        private val generatorBuilder: GeneratorBuilder
) : SmartVDC() {

    private val configObs = generatorBuilder.config(configId)
    private val stateUpdater = StateUpdater(State(configId = configId))
    val state = Observables.combineLatest(stateUpdater.data, configObs)
            .subscribeOn(Schedulers.io())
            .map { (state, config) ->
                val page = when (config.type) {
                    Backup.Type.APP -> State.Page.APP
                    Backup.Type.FILE -> State.Page.FILES
                    null -> State.Page.SELECTION
                }
                state.copy(
                        page = page,
                        configId = configId,
                        existing = config.editor?.existingConfig ?: false,
                        allowSave = config.editor?.allowSave ?: false
                )
            }
            .toLiveData()
    val finishActivity = SingleLiveEvent<Boolean>()

    data class State(
            val configId: UUID,
            val page: Page = Page.SELECTION,
            val existing: Boolean = false,
            val allowSave: Boolean = false,
            val working: Boolean = false
    ) {
        enum class Page(
                val fragmentClass: KClass<out Fragment>
        ) {
            SELECTION(TypeSelectionFragment::class),
            APP(AppEditorFragment::class),
            FILES(FilesEditorFragment::class)
        }
    }

    fun saveConfig() {
        generatorBuilder.save(configId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe { stateUpdater.update { it.copy(working = true) } }
                .doFinally { finishActivity.postValue(true) }
                .subscribe()
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<GeneratorEditorActivityVDC> {
        fun create(handle: SavedStateHandle, configId: UUID): GeneratorEditorActivityVDC
    }
}