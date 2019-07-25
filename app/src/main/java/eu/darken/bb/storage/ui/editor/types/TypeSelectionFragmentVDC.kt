package eu.darken.bb.storage.ui.editor.types

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.main.core.service.BackupService
import eu.darken.bb.storage.core.BackupStorage
import eu.darken.bb.storage.core.StorageManager

class TypeSelectionFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val storageManager: StorageManager,
        @AppContext private val context: Context
) : SmartVDC() {

    val state = storageManager.getSupportedStorageTypes()
            .map { types ->
                State(
                        supportedTypes = types.toList()
                )
            }
            .toLiveData()

    init {

    }

    fun test() {
        context.startService(Intent(context, BackupService::class.java))
    }

    fun createType(type: BackupStorage.Type) {

    }

    data class State(
            val supportedTypes: List<BackupStorage.Type>
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<TypeSelectionFragmentVDC>
}