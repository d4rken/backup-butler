package eu.darken.bb.common.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import eu.darken.bb.backups.BackupId
import java.util.*

class BackupIdAdapter {
    @ToJson
    fun toJson(id: BackupId): String = id.id.toString()

    @FromJson
    fun fromJson(id: String): BackupId = BackupId(UUID.fromString(id))
}