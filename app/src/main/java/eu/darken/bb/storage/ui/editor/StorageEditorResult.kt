package eu.darken.bb.storage.ui.editor

import android.os.Parcelable
import eu.darken.bb.storage.core.Storage
import kotlinx.parcelize.Parcelize

@Parcelize
data class StorageEditorResult(
    val storageId: Storage.Id,
) : Parcelable