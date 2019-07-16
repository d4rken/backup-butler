package eu.darken.bb.backup.repos.local

import android.content.Context
import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.SFile
import eu.darken.bb.common.file.asFile
import eu.darken.bb.common.file.asSFile
import eu.darken.bb.common.file.copyTo
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class LocalStorageRepo(
        private val context: Context,
        private val config: Config
) : BackupRepo {

    private val repoPath = config.path.asFile()

    init {
        if (!repoPath.exists()) {
            if (repoPath.mkdirs()) {
                Timber.tag(TAG).v("Storage path created: %s", repoPath)
            } else {
                Timber.tag(TAG).e("Failed to create repo path: %s", repoPath)
            }
        }
    }

    override fun getAll(): Collection<BackupRepo.BackupRef> {
        TODO("not implemented")
    }

    override fun load(backupRef: BackupRepo.BackupRef): Backup {
        TODO("not implemented")
    }

    override fun save(backup: Backup): BackupRepo.BackupRef {
        val backupDir = File(File(repoPath, backup.name), backup.id.toString())
        val backupRef = LocalStorageRef(
                path = backupDir.asSFile(),
                backupConfig = backup.config
        )

        if (!backupDir.exists()) {
            if (backupDir.mkdirs()) {
                Timber.tag(TAG).v("Backup path created: %s", backupDir)
            } else {
                Timber.tag(TAG).w("Couldn't create backup path (%s): %s", backup, backupDir)
            }
        }

        backup.data.entries.forEach { (key, refs) ->
            refs.forEach {
                val target = File(backupDir, "$key-${it.originalPath!!.name}")
                if (target.exists()) {
                    throw IllegalStateException("File exists: $target")
                }
                it.file.asFile().copyTo(target)
            }
        }
        return backupRef
    }

    override fun remove(backupRef: BackupRepo.BackupRef): Boolean {
        TODO("not implemented")
    }

    override fun toString(): String = "LocalStorageRepo(config=$config)"

    companion object {
        val TAG = App.logTag("StorageRepo", "Local")
    }

    data class LocalStorageRef(
            val path: SFile,
            override val backupConfig: Backup.Config
    ) : BackupRepo.BackupRef

    data class Config(val path: SFile) : BackupRepo.Config {
        override val repoType: BackupRepo.Type
            get() = BackupRepo.Type.LOCAL_STORAGE
    }

    @Reusable
    class Factory @Inject constructor(
            @AppContext private val context: Context
    ) : BackupRepo.Factory {
        override fun isCompatible(config: BackupRepo.Config): Boolean {
            return config.repoType == BackupRepo.Type.LOCAL_STORAGE
        }

        override fun create(config: BackupRepo.Config): BackupRepo {
            config as Config
            return LocalStorageRepo(context, config)
        }

    }
}