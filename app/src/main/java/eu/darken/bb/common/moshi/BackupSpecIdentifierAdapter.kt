package eu.darken.bb.common.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import eu.darken.bb.backup.core.BackupSpec

class BackupSpecIdentifierAdapter {
    @ToJson
    fun toJson(item: BackupSpec.Id): String = item.value

    @FromJson
    fun fromJson(item: String): BackupSpec.Id = BackupSpec.Id(item)
}
