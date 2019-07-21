package eu.darken.bb.backup.repos

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.BackupConfig
import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.backup.repos.local.LocalStorageConfig
import eu.darken.bb.backup.repos.local.LocalStorageRepoReference
import java.util.*
import javax.inject.Qualifier

interface BackupRepo {
    enum class Type {
        LOCAL_STORAGE
    }

    fun getAll(): Collection<BackupReference>

    fun load(backupReference: BackupReference, backupId: BackupId): Backup

    fun save(backup: Backup): BackupReference

    fun remove(backupReference: BackupReference): Boolean

    interface BackupReference {
        val backupConfig: BackupConfig
        val revisionConfig: RevisionConfig
    }

    interface RevisionConfig {
        val revisionType: Type
        val revisions: List<Revision>

        fun getRevision(backupId: BackupId): Revision?

        enum class Type {
            SIMPLE
        }

        companion object {
            val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<RevisionConfig> = PolymorphicJsonAdapterFactory.of(RevisionConfig::class.java, "revisionType")
                    .withSubtype(DefaultRevisionConfig::class.java, Type.SIMPLE.name)
        }

        interface Revision {
            val backupId: BackupId
            val createdAt: Date
        }
    }

    interface RepoReference {
        companion object {
            val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<RepoReference> = PolymorphicJsonAdapterFactory.of(RepoReference::class.java, "repoType")
                    .withSubtype(LocalStorageRepoReference::class.java, Type.LOCAL_STORAGE.name)
        }

        val repoType: Type
    }

    interface RepoConfig {
        companion object {
            val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<RepoConfig> = PolymorphicJsonAdapterFactory.of(RepoConfig::class.java, "repoType")
                    .withSubtype(LocalStorageConfig::class.java, Type.LOCAL_STORAGE.name)
        }

        val repoType: Type
    }

    interface Factory {
        fun isCompatible(repoReference: RepoReference): Boolean

        fun create(repoReference: RepoReference): BackupRepo
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class RepoFactory