package eu.darken.bb.backups.ui.editor

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backups.core.Backup
import eu.darken.bb.backups.core.BackupBuilder
import eu.darken.bb.backups.ui.editor.types.app.AppEditorFragment
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.storage.ui.editor.types.TypeSelectionFragment
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.reflect.KClass


class BackupEditorActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val configId: UUID,
        backupBuilder: BackupBuilder
) : SmartVDC() {

    val finishActivity = SingleLiveEvent<Boolean>()
    val state = backupBuilder
            .config(configId)
            .subscribeOn(Schedulers.io())
            .map { data ->
                val page = when (data.type) {
                    Backup.Type.APP -> State.Page.APP
                    Backup.Type.FILE -> TODO()
                    null -> State.Page.SELECTION
                }
                State(
                        page = page,
                        configId = configId,
                        existing = data.existing
                )
            }
            .toLiveData()

    data class State(
            val configId: UUID,
            val page: Page,
            val existing: Boolean = false
    ) {
        enum class Page(
                val fragmentClass: KClass<out Fragment>
        ) {
            SELECTION(TypeSelectionFragment::class),
            APP(AppEditorFragment::class)
        }
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<BackupEditorActivityVDC> {
        fun create(handle: SavedStateHandle, configId: UUID): BackupEditorActivityVDC
    }
}