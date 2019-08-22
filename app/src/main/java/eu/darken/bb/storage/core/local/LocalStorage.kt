package eu.darken.bb.storage.core.local

import android.content.Context
import com.jakewharton.rx.replayingShare
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
import eu.darken.bb.common.rx.filterUnchanged
import eu.darken.bb.processor.core.tmp.TmpDataRepo
import eu.darken.bb.storage.core.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
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
    private val specAdapter = moshi.adapter(BackupSpec::class.java)
    private val versioningAdapter = moshi.adapter(Versioning::class.java)
    private var storageConfig: LocalStorageConfig

    private val dataDirEvents = Observable.fromCallable { dataDir.listFiles() }
            .onErrorReturnItem(emptyArray())
            .repeatWhen { it.delay(1, TimeUnit.SECONDS) }
            .filterUnchanged { old, new -> old.toList() != new.toList() }
            .replayingShare()

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

    override fun content(): Observable<Collection<Storage.Content>> = contentObs
    private val contentObs: Observable<Collection<Storage.Content>> = dataDirEvents
            .map {
                val content = mutableListOf<Storage.Content>()
                if (!dataDir.exists()) throw MissingFileException(dataDir.asSFile())

                for (backupDir in dataDir.listFiles()) {
                    if (backupDir.isFile) {
                        Timber.tag(TAG).w("Unexpected file within data directory: %s", backupDir)
                        continue
                    }

                    val backupConfig = specAdapter.fromFile(File(backupDir, SPEC_FILE))
                    if (backupConfig == null) {
                        Timber.tag(TAG).e("Dir without spec file: %s", backupDir)
                        continue
                    }

                    val versioning = getVersioning(backupConfig.specId)
                    if (versioning == null) {
                        Timber.tag(TAG).e("Dir without revision file: %s", backupDir)
                        continue
                    }
                    val ref = LocalStorageContent(
                            storageId = storageConfig.storageId,
                            path = backupDir.asSFile(),
                            backupSpec = backupConfig,
                            versioning = versioning
                    )
                    content.add(ref)
                }
                val coll: Collection<Storage.Content> = content.toList()
                return@map coll
            }
            .doOnError { Timber.tag(TAG).e(it) }
            .replayingShare()

    override fun info(): Observable<StorageInfo> = infoObs
    private val infoObs: Observable<StorageInfo> = content()
            .map { contents ->
                var status: StorageInfo.Status? = null
                try {
                    status = StorageInfo.Status(
                            itemCount = contents.size,
                            totalSize = 0,
                            isReadOnly = !dataDir.canWrite()
                    )
                } catch (e: Exception) {
                    Timber.tag(TAG).w(e)
                }
                StorageInfo(
                        ref = repoRef,
                        config = storageConfig,
                        status = status
                )
            }
            .doOnError { Timber.tag(TAG).e(it) }
            .replayingShare()

    override fun details(content: Storage.Content, backupId: Backup.Id): Observable<Storage.Content.Details> {
        content as LocalStorageContent
        val backupDir = content.path.asFile().assertExists()
        val versionDir = File(backupDir, backupId.id.toString()).assertExists()
        return Observable.fromCallable { backupDir.listFiles() }
                .repeatWhen { it.delay(1, TimeUnit.SECONDS) }
                .filterUnchanged()
                .map {
                    val items = mutableListOf<Storage.Content.Item>()

                    versionDir.listFiles().forEach { file ->
                        items.add(object : Storage.Content.Item {
                            override val label: String
                                get() = file.path.substring(versionDir.path.length)
                        })
                    }
                    return@map Storage.Content.Details(items)
                }
                .replayingShare()
    }

    override fun load(content: Storage.Content, backupId: Backup.Id): Backup.Unit {
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

    override fun save(backup: Backup.Unit): Pair<Storage.Content, Versioning.Version> {
        updateProgressPrimary(storageConfig.label + ": " + context.getString(R.string.progress_label_saving))
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        val backupDir = getBackupDir(backup.spec.specId).tryMkDirs()

        val specFile = File(backupDir, SPEC_FILE)
        if (!specFile.exists()) {
            specAdapter.toFile(backup.spec, specFile)
        } else {
            val existingSpec = specAdapter.fromFile(specFile)
            if (existingSpec != backup.spec) {
                throw IllegalStateException("BackupSpec missmatch:\nExisting: $existingSpec\n\nNew: ${backup.spec}")
            }
        }

        val newRevision = SimpleVersioning.Version(backupId = Backup.Id(), createdAt = Date())
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

        var versioning = updateVersioning(backup.spec.specId) { old ->
            old as SimpleVersioning
            old.copy(versions = old.versions.toMutableList().apply { add(newRevision) }.toList())
        }
        Timber.tag(TAG).d("Revision limits: Current=%d, Allowed=%d", versioning.versions.size, backup.spec.revisionLimit)

        while (versioning.versions.size > backup.spec.revisionLimit) {
            val oldest = versioning.versions.minBy { it.createdAt }!!

            Timber.tag(TAG).d("Revision limit execeeded, deleting oldest: %s", oldest)

            val newContent = remove(backup.spec.specId, oldest.backupId).blockingGet()
            versioning = newContent.versioning
        }

        val tempRef = LocalStorageContent(
                path = backupDir.asSFile(),
                storageId = storageConfig.storageId,
                backupSpec = backup.spec,
                versioning = versioning
        )
        Timber.tag(TAG).d("New backup created: %s", tempRef)
        return Pair(tempRef, newRevision)
    }

    override fun remove(specId: BackupSpec.Id, backupId: Backup.Id?): Single<Storage.Content> = content()
            .firstOrError()
            .map { contents ->
                contents.first { it.backupSpec.specId == specId }
            }
            .map { it as LocalStorageContent }
            .map { contentItem ->
                return@map if (backupId != null) {
                    val version = contentItem.versioning.getVersion(backupId) as SimpleVersioning.Version
                    val versionDir = version.getRevDir(getBackupDir(specId))
                    versionDir.deleteAll()

                    val newVersioning = updateVersioning(specId) { old ->
                        old as SimpleVersioning
                        old.copy(versions = old.versions.filterNot { it.backupId == version.backupId })
                    }
                    contentItem.copy(versioning = newVersioning)
                } else {
                    val backupDir = getBackupDir(specId)
                    backupDir.deleteAll()
                    contentItem.copy(versioning = SimpleVersioning())
                }
            }

    override fun detach(): Completable = Completable
            .complete()
            .doOnSubscribe { Timber.i("Detaching %s", repoRef) }

    override fun wipe(): Completable = Completable
            .fromCallable {
                repoRef.path.asFile().deleteAll()
            }
            .doOnSubscribe { Timber.w("Wiping %s", repoRef) }

    private fun getBackupDir(specId: BackupSpec.Id): File {
        return File(dataDir, specId.value)
    }

    private fun getVersioning(specId: BackupSpec.Id): Versioning? {
        val revisionConfigFile = File(getBackupDir(specId), VERSIONING_FILE)
        return versioningAdapter.fromFile(revisionConfigFile)
    }

    private fun updateVersioning(specId: BackupSpec.Id, update: (Versioning) -> Versioning): Versioning {
        val existing = getVersioning(specId)
        val newVersioning = update.invoke(existing ?: SimpleVersioning())
        if (newVersioning != existing) {
            val revisionConfigFile = File(getBackupDir(specId), VERSIONING_FILE)
            versioningAdapter.toFile(newVersioning, revisionConfigFile)
        }
        return newVersioning
    }

    override fun toString(): String = "LocalStorage(storageConfig=$storageConfig)"

    companion object {
        val TAG = App.logTag("StorageRepo", "Local")
        const val SPEC_FILE = "backup.data"
        const val VERSIONING_FILE = "revision.data"
    }

}