package eu.darken.bb.backup.core

import android.content.Context
import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.backup.core.app.AppSpecGenerator
import eu.darken.bb.backup.core.files.FilesSpecGenerator
import eu.darken.bb.common.IdType
import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

interface Generator {

    fun generate(config: Config): Collection<BackupSpec>

    @Keep
    interface Config {
        val generatorId: Id
        val generatorType: Backup.Type
        val label: String

        fun getDescription(context: Context): String

        companion object {
            val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Config> =
                MyPolymorphicJsonAdapterFactory.of(Config::class.java, "generatorType")
                    .withSubtype(AppSpecGenerator.Config::class.java, Backup.Type.APP.name)
                    .withSubtype(FilesSpecGenerator.Config::class.java, Backup.Type.FILES.name)
                    .skipLabelSerialization()
        }
    }

    @Parcelize @Keep
    @JsonClass(generateAdapter = true)
    data class Id(override val value: UUID = UUID.randomUUID()) : Parcelable, IdType<Id> {

        @IgnoredOnParcel @Transient override val idString = value.toString()

        override fun compareTo(other: Id): Int = value.compareTo(other.value)

        override fun toString(): String = "GeneratorId($idString)"
    }
}