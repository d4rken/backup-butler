package eu.darken.bb.backups.ui.editor.types

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backups.core.Backup
import eu.darken.bb.backups.core.BackupBuilder
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.rx.toLiveData
import io.reactivex.schedulers.Schedulers
import java.util.*

class TypeSelectionFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val backupConfig: UUID,
        private val builder: BackupBuilder
) : SmartVDC() {

    val state = builder.getSupportedBackupTypes()
            .map { types ->
                State(
                        supportedTypes = types.toList()
                )
            }
            .toLiveData()

    val finishActivity = SingleLiveEvent<Boolean>()

    fun createType(type: Backup.Type) {
        builder.update(backupConfig) { it!!.copy(type = type) }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    fun dismiss(): Boolean {
        builder.remove(backupConfig)
                .subscribeOn(Schedulers.io())
                .subscribe { _ ->
                    finishActivity.postValue(true)
                }
        return true
    }

    data class State(
            val supportedTypes: List<Backup.Type>
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<TypeSelectionFragmentVDC> {
        fun create(handle: SavedStateHandle, backupConfig: UUID): TypeSelectionFragmentVDC
    }
}