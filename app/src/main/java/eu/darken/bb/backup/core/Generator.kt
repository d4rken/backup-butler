package eu.darken.bb.backup.core

import android.content.Context
import android.os.Parcelable
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.core.app.AppSpecGenerator
import eu.darken.bb.backup.core.files.FilesSpecGenerator
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
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
            val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<Config> = PolymorphicJsonAdapterFactory.of(Config::class.java, "generatorType")
                    .withSubtype(AppSpecGenerator.Config::class.java, Backup.Type.APP.name)
                    .withSubtype(FilesSpecGenerator.Config::class.java, Backup.Type.FILES.name)
        }
    }

    @Parcelize
    data class Id(val id: UUID = UUID.randomUUID()) : Parcelable {
        override fun toString(): String = "GeneratorId($id)"
    }
}