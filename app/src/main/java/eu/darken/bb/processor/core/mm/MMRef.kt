package eu.darken.bb.processor.core.mm

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.IdType
import eu.darken.bb.common.file.core.APath
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
    @JsonClass(generateAdapter = true)
    data class Props(
            val dataType: Type,
            val name: String? = null,
            val originalPath: APath?,
            val symlinkTarget: APath? = null
    ) {

        init {
            require(name != null || originalPath != null) { "Provide name or path!" }
        }

        val label: String
            get() = (originalPath?.path ?: name)!!
    }

    @Keep
    enum class Type {
        FILE, DIRECTORY, SYMBOLIC_LINK
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
