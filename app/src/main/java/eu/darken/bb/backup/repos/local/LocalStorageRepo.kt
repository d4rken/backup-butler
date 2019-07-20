package eu.darken.bb.backup.repos.local

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.backup.repos.DefaultRevisionConfig
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.*
import eu.darken.bb.common.moshi.fromFile
import eu.darken.bb.common.moshi.toFile
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject

class LocalStorageRepo(
        private val context: Context,
        moshi: Moshi,
        repoRef: Ref
) : BackupRepo {
    private val dataPath = File(repoRef.path.asFile(), "data").tryMkDir()
    private val repoConfigFile = File(repoRef.path.asFile(), "repository.config")
    private val repoConfigAdapter = moshi.adapter(Config::class.java)
    private val backupConfigAdapter = moshi.adapter(Backup.Config::class.java)
    private val revisionConfigAdapter = moshi.adapter(BackupRepo.RevisionConfig::class.java)
    private val repoConfig: Config

    init {
        var config = repoConfigAdapter.fromFile(repoConfigFile)
        if (config == null) {
            config = Config(

            )
            repoConfigAdapter.toFile(config, repoConfigFile)
        }
        repoConfig = config
    }

    override fun getAll(): Collection<BackupRepo.BackupRef> {
        val refs = mutableListOf<BackupRepo.BackupRef>()
        for (backupDir in dataPath.listFiles()) {
            if (backupDir.isFile) {
                Timber.tag(TAG).e("Unexpected file within data directory: %s", backupDir)
                continue
            }

            val backupConfig = backupConfigAdapter.fromFile(File(backupDir, BACKUP_CONFIG))
            if (backupConfig == null) {
                Timber.tag(TAG).e("Dir without config file: %s", backupDir)
                continue
            }
            val revisionConfig = revisionConfigAdapter.fromFile(File(backupDir, REVISION_CONFIG)) as? DefaultRevisionConfig
            if (revisionConfig == null) {
                Timber.tag(TAG).e("Dir without revision file: %s", backupDir)
                continue
            }
            val ref = LocalStorageRef(
                    path = backupDir.asSFile(),
                    backupConfig = backupConfig,
                    revisionConfig = revisionConfig
            )
            refs.add(ref)

        }
        return refs
    }

    override fun load(backupRef: BackupRepo.BackupRef): Backup {
        TODO("not implemented")
    }

    override fun save(backup: Backup): BackupRepo.BackupRef {
        val backupDir = File(dataPath, backup.name).tryMkDir()

        val backupConfigFile = File(backupDir, BACKUP_CONFIG)
        if (!backupConfigFile.exists()) {
            backupConfigAdapter.toFile(backup.config, backupConfigFile)
        } else {
            val existingConfig = backupConfigAdapter.fromFile(backupConfigFile)
            if (existingConfig != backup.config) {
                throw IllegalStateException("BackupConfig missmatch:\nExisting: $existingConfig\n\nNew: ${backup.config}")
            }
        }

        val newRevision = DefaultRevisionConfig.Revision(id = backup.id, createdAt = Date())

        val revisionConfigFile = File(backupDir, REVISION_CONFIG)
        val revisionConfig: DefaultRevisionConfig = if (!revisionConfigFile.exists()) {
            DefaultRevisionConfig(revisions = listOf(newRevision))
        } else {
            val existing = revisionConfigAdapter.fromFile(revisionConfigFile) as DefaultRevisionConfig
            existing.copy(revisions = existing.revisions.toMutableList().apply { add(newRevision) }.toList())
        }
        revisionConfigAdapter.toFile(revisionConfig, revisionConfigFile)

        val revisionDir = newRevision.getRevDir(backupDir)
        backup.data.entries.forEach { (key, refs) ->
            refs.forEach {
                val target = File(revisionDir, "$key-${it.originalPath!!.name}")
                if (target.exists()) throw IllegalStateException("File exists: $target")
                it.file.asFile().copyTo(target)
            }
        }

        val backupRef = LocalStorageRef(
                path = backupDir.asSFile(),
                backupConfig = backup.config,
                revisionConfig = revisionConfig
        )
        return backupRef
    }

    override fun remove(backupRef: BackupRepo.BackupRef): Boolean {
        TODO("not implemented")

        // TODO update revision data

        // TODO remove files
    }

    override fun toString(): String = "LocalStorageRepo(repoConfig=$repoConfig)"

    companion object {
        val TAG = App.logTag("StorageRepo", "Local")
        const val BACKUP_CONFIG = "backup.config"
        const val REVISION_CONFIG = "revision.config"
    }

    data class LocalStorageRef(
            val path: SFile,
            override val backupConfig: Backup.Config,
            override val revisionConfig: DefaultRevisionConfig
    ) : BackupRepo.BackupRef

    data class Config(override val repoType: BackupRepo.Type = BackupRepo.Type.LOCAL_STORAGE) : BackupRepo.RepoConfig

    data class Ref(val path: SFile) : BackupRepo.RepoRef {
        override val repoType: BackupRepo.Type
            get() = BackupRepo.Type.LOCAL_STORAGE
    }

    @Reusable
    class Factory @Inject constructor(
            @AppContext private val context: Context,
            private val moshi: Moshi
    ) : BackupRepo.Factory {

        override fun isCompatible(repoRef: BackupRepo.RepoRef): Boolean {
            return repoRef.repoType == BackupRepo.Type.LOCAL_STORAGE
        }

        override fun create(repoRef: BackupRepo.RepoRef): BackupRepo {
            repoRef as Ref
            return LocalStorageRepo(context, moshi, repoRef)
        }

    }
}