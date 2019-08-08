package eu.darken.bb.storage.ui.viewer

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.ui.editor.types.TypeSelectionFragment
import eu.darken.bb.storage.ui.editor.types.local.LocalEditorFragment
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KClass


class StorageEditorActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        storageBuilder: StorageBuilder
) : SmartVDC() {

    val finishActivity = SingleLiveEvent<Boolean>()
    val state = storageBuilder
            .storage(storageId)
            .subscribeOn(Schedulers.io())
            .map { data ->
                val page = when (data.storageType) {
                    Storage.Type.LOCAL -> State.Page.LOCAL
                    Storage.Type.SAF -> TODO()
                    null -> State.Page.SELECTION
                }
                State(
                        page = page,
                        storageId = storageId,
                        existing = data.editor?.isExistingStorage() == true
                )
            }
            .toLiveData()

    data class State(
            val storageId: Storage.Id,
            val page: Page,
            val existing: Boolean = false
    ) {
        enum class Page(
                val fragmentClass: KClass<out Fragment>
        ) {
            SELECTION(TypeSelectionFragment::class),
            LOCAL(LocalEditorFragment::class)
        }
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<StorageEditorActivityVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageEditorActivityVDC
    }
}