package eu.darken.bb.backups.core

import android.content.Context
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backups.core.app.AppBackupSpecGenerator
import io.reactivex.Single
import java.util.*

interface SpecGenerator {

    fun generate(config: Config): Collection<BackupSpec>

    interface Editor {

        val existingConfig: Boolean

        val allowSave: Boolean

        fun save(): Single<Config>

        interface Factory<T : Editor> {
            fun create(generatorId: UUID, parentConfig: Config? = null): T
        }
    }

    interface Config {
        val generatorId: UUID
        val generatorType: Backup.Type
        val label: String

        fun getDescription(context: Context): String

        companion object {
            val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<Config> = PolymorphicJsonAdapterFactory.of(Config::class.java, "generatorType")
                    .withSubtype(AppBackupSpecGenerator.Config::class.java, Backup.Type.APP.name)
        }
    }
}