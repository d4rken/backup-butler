package eu.darken.bb.backup.core

import android.content.Context
import android.os.Parcelable
import eu.darken.bb.backup.core.app.AppSpecGenerator
import eu.darken.bb.backup.core.files.FilesSpecGenerator
import eu.darken.bb.common.IdType
import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

interface Generator {

    fun generate(config: Config): Collection<BackupSpec>

    interface Editor {

        val config: Observable<out Config>

        val existingConfig: Boolean

        fun isValid(): Observable<Boolean>

        fun save(): Single<out Config>

        fun load(config: Config): Completable

        interface Factory<T : Editor> {
            fun create(generatorId: Id): T
        }
    }

    interface Config {
        val generatorId: Id
        val generatorType: Backup.Type
        val label: String

        fun getDescription(context: Context): String

        companion object {
            val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Config> = MyPolymorphicJsonAdapterFactory.of(Config::class.java, "generatorType")
                    .withSubtype(AppSpecGenerator.Config::class.java, Backup.Type.APP.name)
                    .withSubtype(FilesSpecGenerator.Config::class.java, Backup.Type.FILES.name)
                    .skipLabelSerialization()
        }
    }

    @Parcelize
    data class Id(override val value: UUID = UUID.randomUUID()) : Parcelable, IdType<Id> {

        @IgnoredOnParcel @Transient override val idString = value.toString()

        override fun compareTo(other: Id): Int = value.compareTo(other.value)

        override fun toString(): String = "GeneratorId($idString)"
    }
}