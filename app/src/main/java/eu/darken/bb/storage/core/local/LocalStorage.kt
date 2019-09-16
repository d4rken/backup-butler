package eu.darken.bb.storage.core.local

import android.content.Context
import com.jakewharton.rx.replayingShare
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.BaseBackupBuilder
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.file.*
import eu.darken.bb.common.moshi.fromFile
import eu.darken.bb.common.moshi.toFile
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.rx.filterUnchanged
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.storage.core.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


class LocalStorage @AssistedInject constructor(
        @Assisted storageRef: Storage.Ref,
        @AppContext override val context: Context,
        moshi: Moshi,
        configEditorFactory: LocalStorageEditor.Factory,
        private val mmDataRepo: MMDataRepo
) : Storage, HasContext, Progress.Client {

    private val storageRef: LocalStorageRef = storageRef as LocalStorageRef
    private val dataDir = File(this.storageRef.path.asFile(), "data")
    private val specAdapter = moshi.adapter(BackupSpec::class.java)
    private val versioningAdapter = moshi.adapter(Versioning::class.java)
    private val propsAdapter = moshi.adapter(MMRef.Props::class.java)
    private var storageConfig: LocalStorageConfig

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data

    private val dataDirEvents = Observable.fromCallable { dataDir.listFiles() }
            .subscribeOn(Schedulers.io())
            .onErrorReturnItem(emptyArray())
            .repeatWhen { it.delay(1, TimeUnit.SECONDS) }
            .filterUnchanged { old, new -> old.toList() != new.toList() }
            .replayingShare()

    init {
        val configEditor = configEditorFactory.create(this.storageRef.storageId)
        val config = configEditor.load(this.storageRef).map { it as Opt<LocalStorageConfig> }.blockingGet()
        if (config.isNull) throw MissingFileException(this.storageRef.path)
        storageConfig = config.notNullValue()
        dataDir.tryMkDirs()
    }

    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override fun items(): Observable<Collection<Storage.Item>> = itemObs
    private val itemObs: Observable<Collection<Storage.Item>> = dataDirEvents
            .map { files ->
                val content = mutableListOf<Storage.Item>()
                if (!dataDir.exists()) throw MissingFileException(dataDir.asSFile())

                for (backupDir in dataDir.listFiles()) {
                    if (backupDir.isFile) {
                        Timber.tag(TAG).w("Unexpected file within data directory: %s", backupDir)
                        continue
                    }

                    val backupConfig = try {
                        specAdapter.fromFile(File(backupDir, SPEC_FILE))
                    } catch (e: Exception) {
                        Timber.tag(TAG).w(e, "Failed to read specfile")
                        null
                    }
                    if (backupConfig == null) {
                        Timber.tag(TAG).w("Dir without spec file: %s", backupDir)
                        continue
                    }

                    val versioning = getVersioning(backupConfig.specId)
                    if (versioning == null) {
                        Timber.tag(TAG).w("Dir without revision file: %s", backupDir)
                        continue
                    }
                    val ref = LocalStorageItem(
                            storageId = storageConfig.storageId,
                            path = backupDir.asSFile(),
                            backupSpec = backupConfig,
                            versioning = versioning
                    )
                    content.add(ref)
                }
                val coll: Collection<Storage.Item> = content.toList()
                return@map coll
            }
            .doOnError { Timber.tag(TAG).e(it) }
            .doOnSubscribe { Timber.tag(TAG).d("doOnSubscribe().doFinally()") }
            .doFinally { Timber.tag(TAG).d("items().doFinally()") }
            .replayingShare()

    override fun info(): Observable<StorageInfo> = infoObs
    private val infoObs: Observable<StorageInfo> = items()
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
                        ref = this.storageRef,
                        config = storageConfig,
                        status = status
                )
            }
            .doOnError { Timber.tag(TAG).e(it) }
            .doOnSubscribe { Timber.tag(TAG).d("info().doOnSubscribe()") }
            .doFinally { Timber.tag(TAG).d("info().doFinally()") }
            .replayingShare()

    override fun content(item: Storage.Item, backupId: Backup.Id): Observable<Storage.Item.Content> {
        item as LocalStorageItem
        val backupDir = item.path.asFile().requireExists()
        val versionDir = File(backupDir, backupId.idString).requireExists()

        val backupSpec = specAdapter.fromFile(File(backupDir, SPEC_FILE))
        checkNotNull(backupSpec) { "Can't read $backupDir" }

        return Observable.fromCallable { backupDir.listFiles() }
                .repeatWhen { it.delay(1, TimeUnit.SECONDS) }
                .filterUnchanged()
                .map {
                    val items = versionDir.listFiles()
                            .filter { it.path.endsWith(PROP_EXT) }
                            .map { file ->
                                val props = propsAdapter.fromFile(file)
                                checkNotNull(props) { "Can't read props from $file" }
                                object : Storage.Item.Content.Entry {
                                    override val label: String = backupSpec.getContentEntryLabel(props)
                                }
                            }
                            .toList()
                    return@map Storage.Item.Content(items)
                }
                .doOnSubscribe { Timber.tag(TAG).d("content(%s).doOnSubscribe()", backupId) }
                .doOnError { Timber.tag(TAG).w(it, "Failed to get content: item=$item, backupId=$backupId") }
                .doFinally { Timber.tag(TAG).d("content(%s).doFinally()", backupId) }
                .replayingShare()
    }

    override fun load(item: Storage.Item, backupId: Backup.Id): Backup.Unit {
        item as LocalStorageItem
        val backupDir = item.path.asFile().requireExists()

        val version = item.versioning.getVersion(backupId)
        requireNotNull(version) { "BackupReference $item does not contain $backupId" }

        val backupBuilder = BaseBackupBuilder(item.backupSpec, backupId)

        val revisionPath = File(backupDir, version.backupId.idString).requireExists()

        revisionPath.listFiles { file: File -> file.path.endsWith(PROP_EXT) }.forEach { propFile ->
            val prop = propsAdapter.fromFile(propFile)!!
            val tmpRef = mmDataRepo.create(backupId, prop)

            when (tmpRef.type) {
                MMRef.Type.FILE -> {
                    val dataFile = File(propFile.parent, propFile.name.replace(PROP_EXT, DATA_EXT))
                    dataFile.copyTo(tmpRef.tmpPath)
                }
                MMRef.Type.DIRECTORY -> {
                    tmpRef.tmpPath.mkdirs()
                }
            }

            val keySplit = propFile.name.split("#")
            backupBuilder.data.getOrPut(
                    if (keySplit.size == 2) keySplit[0] else "",
                    { mutableListOf() }
            ).add(tmpRef)
        }

        return backupBuilder.toBackup()
    }

    override fun save(backup: Backup.Unit): Pair<Storage.Item, Versioning.Version> {
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
        val revisionDir = newRevision.getRevDir(backupDir).tryMkDirs()

        var current = 0
        val max = backup.data.values.fold(0, { cnt, vals -> cnt + vals.size })

        backup.data.entries.forEach { (baseKey, refs) ->
            refs.forEach { ref ->
                updateProgressSecondary(ref.originalPath.path)
                updateProgressCount(Progress.Count.Counter(++current, max))
                var key = baseKey
                if (key.isNotBlank()) key += "#"

                val targetProp = File(revisionDir, "$key${ref.refId.idString}$PROP_EXT").requireNotExists()
                propsAdapter.toFile(ref.props, targetProp)

                val target = File(revisionDir, "$key${ref.refId.idString}$DATA_EXT").requireNotExists()
                when (ref.type) {
                    MMRef.Type.FILE -> ref.tmpPath.copyTo(target)
                    MMRef.Type.DIRECTORY -> target.mkdir()
                }

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

        val tempRef = LocalStorageItem(
                path = backupDir.asSFile(),
                storageId = storageConfig.storageId,
                backupSpec = backup.spec,
                versioning = versioning
        )
        Timber.tag(TAG).d("New backup created: %s", tempRef)
        return Pair(tempRef, newRevision)
    }

    override fun remove(specId: BackupSpec.Id, backupId: Backup.Id?): Single<Storage.Item> = items()
            .firstOrError()
            .map { contents ->
                contents.first { it.backupSpec.specId == specId }
            }
            .map { it as LocalStorageItem }
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
            .doOnSubscribe { Timber.i("Detaching %s", storageRef) }

    override fun wipe(): Completable = Completable
            .fromCallable {
                storageRef.path.asFile().deleteAll()
            }
            .doOnSubscribe { Timber.w("wipe().doOnSubscribe %s", storageRef) }
            .doFinally { Timber.w("wipe().dofinally%s", storageRef) }

    private fun getBackupDir(specId: BackupSpec.Id): File {
        return File(dataDir, specId.value)
    }

    private fun getVersioning(specId: BackupSpec.Id): Versioning? {
        val revisionConfigFile = File(getBackupDir(specId), VERSIONING_FILE)
        return try {
            versioningAdapter.fromFile(revisionConfigFile)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to get versioning for %s (%s)", revisionConfigFile, specId)
            null
        }
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

    @AssistedInject.Factory
    interface Factory : Storage.Factory<LocalStorage>

    companion object {
        val TAG = App.logTag("Storage", "Local")
        const val DATA_EXT = ".data"
        const val PROP_EXT = ".prop"
        const val SPEC_FILE = "backup.data"
        const val VERSIONING_FILE = "revision.data"
    }

}

internal fun Versioning.Version.getRevDir(base: File): File {
    return File(base, backupId.idString)
}