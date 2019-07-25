package eu.darken.bb.storage.ui.editor

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.storage.core.BackupStorage
import eu.darken.bb.storage.core.StorageRefRepo
import eu.darken.bb.storage.ui.editor.types.TypeSelectionFragment
import eu.darken.bb.storage.ui.editor.types.local.LocalEditorFragment
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.reflect.KClass


class StorageEditorActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: UUID,
        private val storageRefRepo: StorageRefRepo
) : SmartVDC() {

    val finishActivity = SingleLiveEvent<Boolean>()
    val state = storageRefRepo.references
            .subscribeOn(Schedulers.io())
            .map {
                val ref = it[storageId]
                val page = when (ref?.storageType) {
                    BackupStorage.Type.LOCAL_STORAGE -> State.Page.LOCAL
                    null -> State.Page.SELECTION
                }
                State(
                        page = page,
                        storageId = storageId,
                        existing = ref != null
                )
            }
            .toLiveData()

    init {

    }

    private fun dismiss() {
        finishActivity.call()
    }

    data class State(
            val storageId: UUID,
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
        fun create(handle: SavedStateHandle, storageId: UUID): StorageEditorActivityVDC
    }
}