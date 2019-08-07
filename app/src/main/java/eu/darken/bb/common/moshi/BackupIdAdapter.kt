package eu.darken.bb.common.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import eu.darken.bb.backup.core.Backup
import java.util.*

class BackupIdAdapter {
    @ToJson
    fun toJson(id: Backup.Id): String = id.id.toString()

    @FromJson
    fun fromJson(id: String): Backup.Id = Backup.Id(UUID.fromString(id))
}