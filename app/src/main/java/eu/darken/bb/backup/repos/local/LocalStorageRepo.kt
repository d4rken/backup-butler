package eu.darken.bb.backup.repos.local

import dagger.Reusable
import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.common.file.SFile
import javax.inject.Inject

class LocalStorageRepo(
        config: Config
) : BackupRepo {

    override fun getAll(): Collection<BackupRepo.BackupRef> {
        TODO("not implemented")
    }

    override fun load(backupRef: BackupRepo.BackupRef): Backup {
        TODO("not implemented")
    }

    override fun save(backup: Backup): BackupRepo.BackupRef {
        TODO("not implemented")
    }

    override fun remove(backupRef: BackupRepo.BackupRef): Boolean {
        TODO("not implemented")
    }

    data class Config(val path: SFile) : BackupRepo.Config {
        override val repoType: BackupRepo.Type
            get() = BackupRepo.Type.LOCAL_STORAGE
    }

    @Reusable
    class Factory @Inject constructor() : BackupRepo.Factory {
        override fun isCompatible(config: BackupRepo.Config): Boolean {
            TODO("not implemented")
        }

        override fun create(config: BackupRepo.Config): BackupRepo {
            TODO("not implemented")
        }

    }
}