package eu.darken.bb.storage.ui.picker

import android.os.Parcelable
import eu.darken.bb.storage.core.Storage
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoragePickerResult(
    val storageId: Storage.Id,
) : Parcelable