package eu.darken.bb.backup.ui.generator.editor

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.jakewharton.rx.replayingShare
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.ui.generator.editor.types.TypeSelectionFragment
import eu.darken.bb.backup.ui.generator.editor.types.app.AppEditorFragment
import eu.darken.bb.backup.ui.generator.editor.types.files.legacy.LegacyFilesEditorFragment
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KClass


class GeneratorEditorActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val generatorId: Generator.Id,
        private val generatorBuilder: GeneratorBuilder
) : SmartVDC() {

    private val dataObs = generatorBuilder.config(generatorId)
            .subscribeOn(Schedulers.io())
            .replayingShare()

    private val stater = Stater(State(generatorId = generatorId))
    val state = stater.liveData

    val pageEvent = SingleLiveEvent<PageData>()
    val finishActivityEvent = SingleLiveEvent<Any>()

    init {
        dataObs
                .switchMap { data ->
                    if (data.editor != null) data.editor.isValid()
                    else Observable.just(false)
                }
                .subscribe { isValid: Boolean ->
                    stater.update { it.copy(allowSave = isValid) }
                }
                .withScopeVDC(this)

        dataObs
                .map { data ->
                    if (data.editor != null) data.editor.existingConfig else false
                }
                .subscribe { isExisting: Boolean ->
                    stater.update { it.copy(existing = isExisting) }
                }
                .withScopeVDC(this)
        dataObs
                .subscribe { data: GeneratorBuilder.Data ->
                    val p = PageData(data.generatorId, data.generatorType)
                    if (stater.snapshot.currentPage != p.getPage()) {
                        pageEvent.postValue(p)
                        stater.update { it.copy(currentPage = p.getPage()) }
                    }
                }
                .withScopeVDC(this)

        generatorBuilder.builders
                .filter { !it.containsKey(generatorId) }
                .subscribe {
                    finishActivityEvent.postValue(Any())
                }
                .withScopeVDC(this)
    }

    fun saveConfig() {
        generatorBuilder.save(generatorId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe { stater.update { it.copy(working = true) } }
                .doFinally { finishActivityEvent.postValue(Any()) }
                .subscribe()
    }

    data class State(
            val generatorId: Generator.Id,
            val currentPage: PageData.Page? = null,
            val existing: Boolean = false,
            val allowSave: Boolean = false,
            val working: Boolean = false
    )

    data class PageData(val generatorId: Generator.Id, private val type: Generator.Type?) {

        fun getPage(): Page = Page.values().first { it.backupType == type }

        enum class Page(val backupType: Generator.Type?, val fragmentClass: KClass<out Fragment>) {
            SELECTION(null, TypeSelectionFragment::class),
            APP(Generator.Type.APP, AppEditorFragment::class),
            FILES_LEGACY(Generator.Type.FILE_LEGACY, LegacyFilesEditorFragment::class)
        }

    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<GeneratorEditorActivityVDC> {
        fun create(handle: SavedStateHandle, generatorId: Generator.Id): GeneratorEditorActivityVDC
    }
}