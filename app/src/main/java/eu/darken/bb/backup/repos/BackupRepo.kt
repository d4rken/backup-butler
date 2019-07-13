package eu.darken.bb.backup.repos

import eu.darken.bb.backup.backups.Backup
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
    }

    interface Config {
        val repoType: Type
    }

    interface Factory {
        fun isCompatible(config: Config): Boolean

        fun create(config: Config): BackupRepo
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class RepoFactory