package eu.darken.bb.backup.backups

import androidx.annotation.Keep
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.backups.app.AppBackup
import eu.darken.bb.backup.backups.file.FileBackup
import eu.darken.bb.backup.processor.tmp.TmpRef
import java.util.*
import javax.inject.Qualifier

interface Backup {
    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<Backup> = PolymorphicJsonAdapterFactory.of(Backup::class.java, "backupType")
                .withSubtype(AppBackup::class.java, Type.APP_BACKUP.name)
                .withSubtype(FileBackup::class.java, Type.FILE.name)
    }

    @Keep
    enum class Type {
        APP_BACKUP, FILE
    }

    val name: String
    val id: BackupId
    val backupType: Type
    val config: Config
    val data: Map<String, Collection<TmpRef>>

    interface Endpoint {
        fun backup(config: Config): Backup

        fun restore(backup: Backup): Boolean

        interface Factory {
            fun isCompatible(config: Config): Boolean

            fun create(config: Config): Endpoint
        }
    }

    interface Config {
        companion object {
            val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<Config> = PolymorphicJsonAdapterFactory.of(Config::class.java, "configType")
                    .withSubtype(AppBackup.Config::class.java, Type.APP_BACKUP.name)
                    .withSubtype(FileBackup.Config::class.java, Type.FILE.name)
        }

        val configType: Type
    }
}

class BackupId(val id: UUID = UUID.randomUUID()) {
    override fun toString(): String = id.toString()
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class EndpointFactory