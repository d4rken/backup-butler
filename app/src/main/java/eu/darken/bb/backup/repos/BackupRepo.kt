package eu.darken.bb.backup.repos

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.BackupId
import java.util.*
import javax.inject.Qualifier

interface BackupRepo {
    enum class Type {
        LOCAL_STORAGE
    }

    fun getAll(): Collection<BackupRef>

    fun load(backupRef: BackupRef): Backup

    fun save(backup: Backup): BackupRef

    fun remove(backupRef: BackupRef): Boolean

    interface BackupRef {
        val backupConfig: Backup.Config
        val revisionConfig: RevisionConfig
    }

    interface RevisionConfig {
        companion object {
            val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<RevisionConfig> = PolymorphicJsonAdapterFactory.of(RevisionConfig::class.java, "configType")
                    .withSubtype(DefaultRevisionConfig::class.java, Type.SIMPLE.name)
        }

        val revisions: List<Revision>
        val configType: Type

        enum class Type {
            SIMPLE
        }

        interface Revision {
            val id: BackupId
            val createdAt: Date
        }
    }

    interface RepoRef {
        val repoType: Type
    }

    interface RepoConfig {
        val repoType: Type
    }

    interface Factory {
        fun isCompatible(repoRef: RepoRef): Boolean

        fun create(repoRef: RepoRef): BackupRepo
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class RepoFactory