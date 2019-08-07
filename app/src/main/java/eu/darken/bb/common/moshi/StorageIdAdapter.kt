package eu.darken.bb.common.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import eu.darken.bb.storage.core.BackupStorage
import java.util.*

class StorageIdAdapter {
    @ToJson
    fun toJson(item: BackupStorage.Id): String = item.id.toString()

    @FromJson
    fun fromJson(item: String): BackupStorage.Id = BackupStorage.Id(UUID.fromString(item))
}
