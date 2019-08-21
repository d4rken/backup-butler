package eu.darken.bb.backup.ui.generator.editor

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.ui.generator.editor.types.TypeSelectionFragment
import eu.darken.bb.backup.ui.generator.editor.types.app.AppEditorFragment
import eu.darken.bb.backup.ui.generator.editor.types.files.FilesEditorFragment
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
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

    private val validObs: Observable<Boolean> = dataObs
            .switchMap { data ->
                if (data.editor != null) data.editor.isValid()
                else Observable.just(false)
            }
            .doOnNext { isValid ->
                stater.update { it.copy(allowSave = isValid) }
            }

    private val existingObs: Observable<Boolean> = dataObs
            .map { data ->
                if (data.editor != null) data.editor.existingConfig
                else false
            }
            .doOnNext { isExisting ->
                stater.update { it.copy(existing = isExisting) }
            }

    private val pageObs: Observable<GeneratorBuilder.Data> = dataObs.doOnNext { data ->
        val p = PageData(data.generatorId, data.generatorType)
        if (stater.snapshot.currentPage != p.getPage()) {
            pageEvent.postValue(p)
            stater.update { it.copy(currentPage = p.getPage()) }
        }
    }

    private val stater = Stater(State(generatorId = generatorId))
            .addLiveDep {
                validObs.subscribe()
                existingObs.subscribe()
                pageObs.subscribe()
            }

    val state = stater.liveData
    val pageEvent = SingleLiveEvent<PageData>()
    val finishActivity = SingleLiveEvent<Boolean>()

    init {

    }

    fun saveConfig() {
        generatorBuilder.save(generatorId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe { stater.update { it.copy(working = true) } }
                .doFinally { finishActivity.postValue(true) }
                .subscribe()
    }

    data class State(
            val generatorId: Generator.Id,
            val currentPage: PageData.Page? = null,
            val existing: Boolean = false,
            val allowSave: Boolean = false,
            val working: Boolean = false
    )

    data class PageData(val generatorId: Generator.Id, private val type: Backup.Type?) {

        fun getPage(): Page = Page.values().first { it.backupType == type }

        enum class Page(val backupType: Backup.Type?, val fragmentClass: KClass<out Fragment>) {
            SELECTION(null, TypeSelectionFragment::class),
            APP(Backup.Type.APP, AppEditorFragment::class),
            FILES(Backup.Type.FILE, FilesEditorFragment::class)
        }

    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<GeneratorEditorActivityVDC> {
        fun create(handle: SavedStateHandle, generatorId: Generator.Id): GeneratorEditorActivityVDC
    }
}