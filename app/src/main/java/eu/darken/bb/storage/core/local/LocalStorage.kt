package eu.darken.bb.storage.core.local

import android.content.Context
import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.backups.Backup
import eu.darken.bb.backups.BackupConfig
import eu.darken.bb.backups.BackupId
import eu.darken.bb.backups.BaseBackupBuilder
import eu.darken.bb.common.Opt
import eu.darken.bb.common.file.*
import eu.darken.bb.common.moshi.fromFile
import eu.darken.bb.common.moshi.toFile
import eu.darken.bb.processor.tmp.TmpDataRepo
import eu.darken.bb.storage.core.*
import io.reactivex.Observable
import timber.log.Timber
import java.io.File
import java.util.*

class LocalStorage(
        private val context: Context,
        moshi: Moshi,
        private val configEditorFactory: LocalStorageEditor.Factory,
        private val tmpDataRepo: TmpDataRepo,
        private val repoRef: LocalStorageRef
) : BackupStorage {
    private val dataDir = File(repoRef.path.asFile(), "data")
    private val backupConfigAdapter = moshi.adapter(BackupConfig::class.java)
    private val revisionConfigAdapter = moshi.adapter(RevisionConfig::class.java)
    private val repoConfig: LocalStorageConfig

    init {
        val configEditor = configEditorFactory.create(repoRef.storageId)
        val config = configEditor.load(repoRef).map { it as Opt<LocalStorageConfig> }.blockingGet()
        if (config.isNull) throw MissingFileException(repoRef.path)
        repoConfig = config.notNullValue()
        dataDir.tryMkDirs()
    }

    override fun info(): Observable<StorageInfo> {
        return Observable.just(StorageInfo(
                ref = repoRef,
                config = repoConfig
        ))
    }

    override fun getAll(): Collection<BackupReference> {
        val refs = mutableListOf<BackupReference>()
        for (backupDir in dataDir.listFiles()) {
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
            val ref = LocalStorageBackupReference(
                    path = backupDir.asSFile(),
                    backupConfig = backupConfig,
                    revisionConfig = revisionConfig
            )
            refs.add(ref)
        }
        return refs
    }

    override fun load(backupReference: BackupReference, backupId: BackupId): Backup {
        backupReference as LocalStorageBackupReference
        val backupDir = backupReference.path.asFile().assertExists()

        val revision = backupReference.revisionConfig.getRevision(backupId)
        if (revision == null) {
            throw IllegalArgumentException("BackupReference $backupReference does not contain $backupId")
        }

        val backupBuilder = BaseBackupBuilder(backupReference.backupConfig, backupId)

        val revisionPath = File(backupDir, revision.backupId.toString()).assertExists()

        revisionPath.listFiles().forEach { file ->
            val tmpRef = tmpDataRepo.create(backupId)
            tmpRef.originalPath = file.asSFile()
            file.copyTo(tmpRef.file.asFile())
            backupBuilder.data.getOrPut(file.name.split("-")[0], { mutableListOf() }).add(tmpRef)
        }

        return backupBuilder.toBackup()
    }

    override fun save(backup: Backup): BackupReference {
        val backupDir = File(dataDir, backup.config.label).tryMkDirs()

        val backupConfigFile = File(backupDir, BACKUP_CONFIG)
        if (!backupConfigFile.exists()) {
            backupConfigAdapter.toFile(backup.config, backupConfigFile)
        } else {
            val existingConfig = backupConfigAdapter.fromFile(backupConfigFile)
            if (existingConfig != backup.config) {
                throw IllegalStateException("BackupConfig missmatch:\nExisting: $existingConfig\n\nNew: ${backup.config}")
            }
        }

        val newRevision = DefaultRevisionConfig.Revision(backupId = backup.id, createdAt = Date())

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

        val backupRef = LocalStorageBackupReference(
                path = backupDir.asSFile(),
                backupConfig = backup.config,
                revisionConfig = revisionConfig
        )
        return backupRef
    }

    override fun remove(backupReference: BackupReference): Boolean {
        TODO("not implemented")

        // TODO update revision data

        // TODO remove files
    }

    override fun toString(): String = "LocalStorage(repoConfig=$repoConfig)"

    companion object {
        val TAG = App.logTag("StorageRepo", "Local")
        const val BACKUP_CONFIG = "backup.config"
        const val REVISION_CONFIG = "revision.config"
    }

}