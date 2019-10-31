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
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.HotData
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
import eu.darken.bb.processor.core.mm.MMRef.Type.*
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.saf.SAFStorage
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit


class LocalStorage @AssistedInject constructor(
        @Assisted storageRef: Storage.Ref,
        @Assisted storageConfig: Storage.Config,
        @AppContext override val context: Context,
        moshi: Moshi,
        private val mmDataRepo: MMDataRepo
) : Storage, HasContext, Progress.Client {

    private val storageRef: LocalStorageRef = storageRef as LocalStorageRef
    override val storageConfig: LocalStorageConfig = storageConfig as LocalStorageConfig
    private val dataDir = File(this.storageRef.path.asFile(), "data")

    private val specAdapter = moshi.adapter(BackupSpec::class.java)
    private val metaDataAdapter = moshi.adapter(Backup.MetaData::class.java)

    private val propsAdapter = moshi.adapter(MMRef.Props::class.java)

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data

    private val dataDirEvents = Observable
            .fromCallable { dataDir.safeListFiles() }
            .subscribeOn(Schedulers.io())
            .onErrorReturnItem(emptyArray())
            .repeatWhen { it.delay(1, TimeUnit.SECONDS) }
            .filterUnchanged { old, new -> old.toList() != new.toList() }
            .replayingShare()

    init {
        Timber.tag(SAFStorage.TAG).i("init(storageRef=%s, storageConfig=%s)", storageRef, storageConfig)
        if (!dataDir.exists()) {
            Timber.tag(TAG).w("Data dir doesn't exist: %s", dataDir)
        }
    }

    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override fun specInfo(specId: BackupSpec.Id): Observable<BackupSpec.Info> = specInfos()
            .map { specInfos ->
                val item = specInfos.find { it.backupSpec.specId == specId }
                requireNotNull(item) { "Can't find backup item for specId $specId" }
            }

    override fun specInfos(): Observable<Collection<BackupSpec.Info>> = itemObs
    private val itemObs: Observable<Collection<BackupSpec.Info>> = dataDirEvents
            .map { files ->
                val content = mutableListOf<BackupSpec.Info>()

                for (backupDir in files) {
                    if (backupDir.isFile) {
                        Timber.tag(TAG).w("Unexpected file within data directory: %s", backupDir)
                        continue
                    }

                    val backupConfig = try {
                        readSpec(BackupSpec.Id(backupDir.name))
                    } catch (e: Throwable) {
                        if (e is InterruptedIOException) throw e
                        Timber.tag(TAG).w("Dir without spec file: %s", backupDir)
                        continue
                    }

                    val metaDatas = getMetaDatas(backupConfig.specId)
                    if (metaDatas.isEmpty()) {
                        Timber.tag(TAG).w("Dir without backups? %s", backupDir)
                        continue
                    }

                    val ref = LocalStorageSpecInfo(
                            storageId = storageConfig.storageId,
                            path = backupDir.asSFile(),
                            backupSpec = backupConfig,
                            backups = metaDatas
                    )
                    content.add(ref)
                }
                return@map content.toList() as Collection<BackupSpec.Info>
            }
            .doOnError { Timber.tag(TAG).e(it) }
            .doOnSubscribe { Timber.tag(TAG).d("doOnSubscribe().doFinally()") }
            .doFinally { Timber.tag(TAG).d("specInfos().doFinally()") }
            .replayingShare()

    override fun info(): Observable<Storage.Info> = infoObs
    private val infoObs: Observable<Storage.Info> = specInfos()
            .map { contents ->
                var status: Storage.Info.Status? = null
                try {
                    status = Storage.Info.Status(
                            itemCount = contents.size,
                            totalSize = 0,
                            isReadOnly = dataDir.exists() && !dataDir.canWrite()
                    )
                } catch (e: Exception) {
                    Timber.tag(TAG).w(e)
                }

                Storage.Info(
                        storageId = this.storageRef.storageId,
                        storageType = this.storageRef.storageType,
                        config = storageConfig,
                        status = status
                )
            }
            .doOnError { Timber.tag(TAG).e(it) }
            .doOnSubscribe { Timber.tag(TAG).d("info().doOnSubscribe()") }
            .doFinally { Timber.tag(TAG).d("info().doFinally()") }
            .replayingShare()

    override fun backupInfo(specId: BackupSpec.Id, backupId: Backup.Id): Observable<Backup.Info> = backupContent(specId, backupId)
            .map { Backup.Info(it.storageId, it.spec, it.metaData) }

    override fun backupContent(specId: BackupSpec.Id, backupId: Backup.Id): Observable<Backup.ContentInfo> = specInfo(specId)
            .flatMap { item ->
                item as LocalStorageSpecInfo
                val backupDir = item.path.asFile().requireExists()
                val versionDir = File(backupDir, backupId.idString).requireExists()
                val backupSpec = readSpec(specId)
                val metaData = readBackupMeta(item.specId, backupId)

                return@flatMap Observable.fromCallable { backupDir.safeListFiles() }
                        .repeatWhen { it.delay(1, TimeUnit.SECONDS) }
                        .filterUnchanged()
                        .map {
                            return@map versionDir.safeListFiles()
                                    .filter { it.path.endsWith(PROP_EXT) }
                                    .map { file ->
                                        val props = propsAdapter.fromFile(file)
                                        Backup.ContentInfo.PropsEntry(backupSpec, metaData, props)
                                    }
                                    .toList()
                        }
                        .onErrorReturnItem(emptyList())
                        .map {
                            return@map Backup.ContentInfo(
                                    storageId = storageId,
                                    spec = backupSpec,
                                    metaData = metaData,
                                    items = it
                            )
                        }
            }
            .doOnSubscribe { Timber.tag(TAG).d("content(%s).doOnSubscribe()", backupId) }
            .doOnError { Timber.tag(SAFStorage.TAG).w(it, "Failed to get content: specId=$specId, backupId=$backupId") }
            .doFinally { Timber.tag(TAG).d("content(%s).doFinally()", backupId) }
            .replayingShare()

    override fun load(specId: BackupSpec.Id, backupId: Backup.Id): Backup.Unit {
        val item = specInfo(specId).blockingFirst()
        item as LocalStorageSpecInfo

        val metaData = readBackupMeta(specId, backupId)
        requireNotNull(metaData) { "BackupReference $item does not have a metadata file" }

        val dataMap = mutableMapOf<String, MutableList<MMRef>>()

        val versionPath = getVersionDir(specId, backupId).requireExists()
        versionPath.safeListFiles { file: File -> file.path.endsWith(PROP_EXT) }.forEach { propFile ->
            val prop = propsAdapter.fromFile(propFile)!!
            val dataFile = File(propFile.parent, propFile.name.replace(PROP_EXT, DATA_EXT))

            val tmpRef = mmDataRepo.create(backupId, prop)

            if (dataFile.isDirectory) {
                tmpRef.tmpPath.mkdirs()
            } else {
                dataFile.copyTo(tmpRef.tmpPath)
            }

            val keySplit = propFile.name.split("#")
            dataMap.getOrPut(
                    if (keySplit.size == 2) keySplit[0] else "",
                    { mutableListOf() }
            ).add(tmpRef)
        }

        return Backup.Unit(
                spec = item.backupSpec,
                metaData = metaData,
                data = dataMap
        )
    }

    override fun save(backup: Backup.Unit): Backup.Info {
        updateProgressPrimary(storageConfig.label + ": " + context.getString(R.string.progress_label_saving))
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        try {
            val existingSpec = readSpec(backup.specId)
            check(existingSpec == backup.spec) { "BackupSpec missmatch:\nExisting: $existingSpec\n\nNew: ${backup.spec}" }
        } catch (e: Throwable) {
            Timber.tag(TAG).d("Reading existing spec failed (${e.message}, creating new one.")
            getSpecDir(backup.specId).tryMkDirs()
            writeSpec(backup.specId, backup.spec)
        }

        val versionDir = getVersionDir(specId = backup.specId, backupId = backup.backupId).tryMkDirs()

        var current = 0
        val max = backup.data.values.fold(0, { cnt, vals -> cnt + vals.size })

        // TODO check that backup dir doesn't exist, ie version dir?
        backup.data.entries.forEach { (baseKey, refs) ->
            refs.forEach { ref ->
                updateProgressSecondary(ref.originalPath.path)
                updateProgressCount(Progress.Count.Counter(++current, max))
                var key = baseKey
                if (key.isNotBlank()) key += "#"

                val targetProp = File(versionDir, "$key${ref.refId.idString}$PROP_EXT").requireNotExists()
                propsAdapter.toFile(ref.props, targetProp)

                val target = File(versionDir, "$key${ref.refId.idString}$DATA_EXT").requireNotExists()
                when (ref.type) {
                    FILE -> ref.tmpPath.copyTo(target)
                    DIRECTORY -> target.mkdir()
                    UNUSED -> throw IllegalStateException("Ref is unused: ${ref.tmpPath}")
                }
            }
        }

        writeBackupMeta(backup.specId, backup.backupId, backup.metaData)

        val info = Backup.Info(
                storageId = storageId,
                spec = backup.spec,
                metaData = backup.metaData
        )
        Timber.tag(SAFStorage.TAG).d("New backup created: %s", info)
        return info
    }

    override fun remove(specId: BackupSpec.Id, backupId: Backup.Id?): Single<BackupSpec.Info> = specInfo(specId)
            .firstOrError()
            .map { specInfo ->
                specInfo as LocalStorageSpecInfo
                if (backupId != null) {
                    val versionDir = getVersionDir(specId, backupId)
                    versionDir.deleteAll()
                } else {
                    val backupDir = getSpecDir(specId)
                    backupDir.deleteAll()
                }
                val newMetaData = if (backupId != null) {
                    // Not a complete deletion
                    getMetaDatas(specId)
                } else {
                    emptySet()
                }
                return@map specInfo.copy(backups = newMetaData)
            }

    override fun detach(wipe: Boolean): Completable = Completable
            .fromCallable {
                if (wipe) storageRef.path.asFile().deleteAll()
            }
            .doOnSubscribe { Timber.w("detach(wipe=%b).doOnSubscribe %s", wipe, storageRef) }
            .doFinally { Timber.w("detach(wipe=%b).dofinally %s", wipe, storageRef) }


    private fun getSpecDir(specId: BackupSpec.Id): File = File(dataDir, specId.value)

    private fun getVersionDir(specId: BackupSpec.Id, backupId: Backup.Id): File = File(getSpecDir(specId), backupId.idString)

    private fun getMetaDatas(specId: BackupSpec.Id): Collection<Backup.MetaData> {
        val metaDatas = mutableListOf<Backup.MetaData>()
        getSpecDir(specId).safeListFiles().filter { it.isDirectory }.forEach { dir ->
            val metaData = readBackupMeta(specId, Backup.Id(dir.name))
            metaDatas.add(metaData)
        }
        return metaDatas
    }

    private fun readBackupMeta(specId: BackupSpec.Id, backupId: Backup.Id): Backup.MetaData {
        val versionFile = File(getVersionDir(specId, backupId), BACKUP_META_FILE)
        return try {
            metaDataAdapter.fromFile(versionFile)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to get metadata from ", versionFile)
            throw e
        }
    }

    private fun writeBackupMeta(specId: BackupSpec.Id, backupId: Backup.Id, metaData: Backup.MetaData) {
        val versionFile = File(getVersionDir(specId, backupId), BACKUP_META_FILE)
        metaDataAdapter.toFile(metaData, versionFile)
    }

    private fun readSpec(specId: BackupSpec.Id): BackupSpec {
        val specFile = File(getSpecDir(specId), SPEC_FILE)
        return try {
            specAdapter.fromFile(specFile)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to get backup spec from ", specFile)
            throw e
        }
    }

    private fun writeSpec(specId: BackupSpec.Id, spec: BackupSpec) {
        val specFile = File(getSpecDir(specId), SPEC_FILE)
        specAdapter.toFile(spec, specFile)
    }

    override fun toString(): String = "LocalStorage(storageRef=$storageRef)"

    @AssistedInject.Factory
    interface Factory : Storage.Factory<LocalStorage>

    companion object {
        internal val TAG = App.logTag("Storage", "Local")
        private const val DATA_EXT = ".data"
        private const val PROP_EXT = ".prop.json"
        private const val SPEC_FILE = "spec.json"
        private const val BACKUP_META_FILE = "backup.json"
    }
}