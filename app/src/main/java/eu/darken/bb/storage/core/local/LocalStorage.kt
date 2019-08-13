package eu.darken.bb.storage.core.local

import android.content.Context
import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.BaseBackupBuilder
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.Opt
import eu.darken.bb.common.file.*
import eu.darken.bb.common.moshi.fromFile
import eu.darken.bb.common.moshi.toFile
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.tmp.TmpDataRepo
import eu.darken.bb.storage.core.*
import io.reactivex.Observable
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


class LocalStorage(
        override val context: Context,
        moshi: Moshi,
        private val configEditorFactory: LocalStorageEditor.Factory,
        private val tmpDataRepo: TmpDataRepo,
        private val repoRef: LocalStorageRef,
        private val progressClient: Progress.Client?
) : Storage, HasContext, Progress.Client {
    private val dataDir = File(repoRef.path.asFile(), "data")
    private val backupConfigAdapter = moshi.adapter(BackupSpec::class.java)
    private val revisionConfigAdapter = moshi.adapter(Versioning::class.java)
    private val storageConfig: LocalStorageConfig

    init {
        val configEditor = configEditorFactory.create(repoRef.storageId)
        val config = configEditor.load(repoRef).map { it as Opt<LocalStorageConfig> }.blockingGet()
        if (config.isNull) throw MissingFileException(repoRef.path)
        storageConfig = config.notNullValue()
        dataDir.tryMkDirs()
    }

    override fun updateProgress(update: (Progress.Data) -> Progress.Data) {
        progressClient?.updateProgress(update)
    }

    override fun info(): Observable<StorageInfo> = Observable
            .create<StorageInfo> { emitter ->
                var status: StorageInfo.Status? = null
                try {
                    val content = content().blockingFirst()
                    status = StorageInfo.Status(content.size, 0)
                } catch (e: Exception) {
                    Timber.tag(TAG).w(e)
                }
                emitter.onNext(StorageInfo(
                        ref = repoRef,
                        config = storageConfig,
                        status = status
                ))
                emitter.onComplete()
            }
            // TODO make nicer, maybe with filesystem observer?
            .repeatWhen { it.delay(3, TimeUnit.SECONDS) }
            .doOnDispose {
                Timber.i("Dispose")
            }
            .doOnSubscribe {
                Timber.i("SUBSCRIBE: %s", it)
            }

    override fun content(): Observable<Collection<Storage.Content>> = Observable.fromCallable {
        val content = mutableListOf<Storage.Content>()
        if (!dataDir.exists()) throw MissingFileException(dataDir.asSFile())

        for (backupDir in dataDir.listFiles()) {
            if (backupDir.isFile) {
                Timber.tag(TAG).e("Unexpected file within data directory: %s", backupDir)
                continue
            }

            val backupConfig = backupConfigAdapter.fromFile(File(backupDir, BACKUP_CONFIG))
            if (backupConfig == null) {
                Timber.tag(TAG).e("Dir without spec file: %s", backupDir)
                continue
            }
            val revisionConfig = revisionConfigAdapter.fromFile(File(backupDir, REVISION_CONFIG)) as? SimpleVersioning
            if (revisionConfig == null) {
                Timber.tag(TAG).e("Dir without revision file: %s", backupDir)
                continue
            }
            val ref = LocalStorageContent(
                    storageId = storageConfig.storageId,
                    path = backupDir.asSFile(),
                    backupSpec = backupConfig,
                    versioning = revisionConfig
            )
            content.add(ref)
        }
        return@fromCallable content
    }

    override fun details(content: Storage.Content, backupId: Backup.Id): Observable<Backup.Details> = Observable.fromCallable {
        content as LocalStorageContent
        val backupDir = content.path.asFile().assertExists()
        val versionDir = File(backupDir, backupId.id.toString()).assertExists()

        val items = mutableListOf<Backup.Item>()

        versionDir.listFiles().forEach { file ->
            items.add(object : Backup.Item {
                override val label: String
                    get() = file.path.substring(versionDir.path.length)
            })
        }
        return@fromCallable Backup.Details(items)
    }.delay(2, TimeUnit.SECONDS)

    override fun load(content: Storage.Content, backupId: Backup.Id): Backup {
        content as LocalStorageContent
        val backupDir = content.path.asFile().assertExists()

        val version = content.versioning.getVersion(backupId)
        if (version == null) {
            throw IllegalArgumentException("BackupReference $content does not contain $backupId")
        }

        val backupBuilder = BaseBackupBuilder(content.backupSpec, backupId)

        val revisionPath = File(backupDir, version.backupId.toString()).assertExists()

        revisionPath.listFiles().forEach { file ->
            val tmpRef = tmpDataRepo.create(backupId)
            tmpRef.originalPath = file.asSFile()
            file.copyTo(tmpRef.file.asFile())
            backupBuilder.data.getOrPut(file.name.split("-")[0], { mutableListOf() }).add(tmpRef)
        }

        return backupBuilder.toBackup()
    }

    override fun save(backup: Backup): Pair<Storage.Content, Versioning.Version> {
        updateProgressPrimary(R.string.saving_to_storage)
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        val backupDir = File(dataDir, backup.spec.specId.value).tryMkDirs()

        val backupConfigFile = File(backupDir, BACKUP_CONFIG)
        if (!backupConfigFile.exists()) {
            backupConfigAdapter.toFile(backup.spec, backupConfigFile)
        } else {
            val existingConfig = backupConfigAdapter.fromFile(backupConfigFile)
            if (existingConfig != backup.spec) {
                throw IllegalStateException("BackupSpec missmatch:\nExisting: $existingConfig\n\nNew: ${backup.spec}")
            }
        }

        val newRevision = SimpleVersioning.Version(backupId = Backup.Id(), createdAt = Date())

        val revisionConfigFile = File(backupDir, REVISION_CONFIG)
        val revisionConfig: SimpleVersioning = if (!revisionConfigFile.exists()) {
            SimpleVersioning(versions = listOf(newRevision))
        } else {
            val existing = revisionConfigAdapter.fromFile(revisionConfigFile) as SimpleVersioning
            existing.copy(versions = existing.versions.toMutableList().apply { add(newRevision) }.toList())
        }
        revisionConfigAdapter.toFile(revisionConfig, revisionConfigFile)

        val revisionDir = newRevision.getRevDir(backupDir)

        var current = 0
        val max = backup.data.values.fold(0, { cnt, vals -> cnt + vals.size })

        backup.data.entries.forEach { (key, refs) ->
            refs.forEach {
                updateProgressSecondary(it.originalPath?.path ?: it.file.path)
                updateProgressCount(Progress.Count.Counter(++current, max))
                val target = File(revisionDir, "$key-${it.originalPath!!.name}")
                if (target.exists()) throw IllegalStateException("File exists: $target")
                it.file.asFile().copyTo(target)
            }
        }

        val backupRef = LocalStorageContent(
                path = backupDir.asSFile(),
                storageId = storageConfig.storageId,
                backupSpec = backup.spec,
                versioning = revisionConfig
        )
        return Pair(backupRef, newRevision)
    }

    override fun remove(content: Storage.Content, backupId: Backup.Id?): Boolean {
        TODO("not implemented")

        // TODO update revision data

        // TODO remove files
    }

    override fun toString(): String = "LocalStorage(storageConfig=$storageConfig)"

    companion object {
        val TAG = App.logTag("StorageRepo", "Local")
        const val BACKUP_CONFIG = "backup.data"
        const val REVISION_CONFIG = "revision.data"
    }

}