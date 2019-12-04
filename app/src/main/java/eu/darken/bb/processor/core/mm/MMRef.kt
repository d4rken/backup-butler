package eu.darken.bb.processor.core.mm

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.IdType
import okio.Source
import java.util.*

data class MMRef(
        val refId: Id,
        val backupId: Backup.Id,
        val source: RefSource
) {

    val props: Props
        get() = source.props

    interface RefSource {
        val props: Props
        fun open(): Source
        fun release()
    }

    @Keep
    enum class Type {
        FILE, DIRECTORY, SYMLINK, ARCHIVE
    }

    @Keep
    @JsonClass(generateAdapter = true)
    data class Id(override val value: UUID = UUID.randomUUID()) : IdType<Id> {

        override val idString = value.toString()

        override fun compareTo(other: Id): Int = value.compareTo(other.value)

        override fun toString(): String = "MMRef.Id($idString)"
    }


    data class Request(
            val backupId: Backup.Id,
            val source: RefSource
    )

}
