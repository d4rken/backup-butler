package eu.darken.bb.storage.core.saf

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
import eu.darken.bb.common.file.core.asFile
import eu.darken.bb.common.file.core.local.copyTo
import eu.darken.bb.common.file.core.local.deleteAll
import eu.darken.bb.common.file.core.saf.*
import eu.darken.bb.common.moshi.fromSAFFile
import eu.darken.bb.common.moshi.toSAFFile
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.rx.filterUnchanged
import eu.darken.bb.common.rx.onErrorMixLast
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.MMRef.Type.DIRECTORY
import eu.darken.bb.processor.core.mm.MMRef.Type.FILE
import eu.darken.bb.storage.core.Storage
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit


class SAFStorage @AssistedInject constructor(
        @Assisted storageRef: Storage.Ref,
        @Assisted storageConfig: Storage.Config,
        @AppContext override val context: Context,
        moshi: Moshi,
        private val safGateway: SAFGateway,
        private val mmDataRepo: MMDataRepo
) : Storage, HasContext, Progress.Client {

    private val storageRef: SAFStorageRef = storageRef as SAFStorageRef
    override val storageConfig: SAFStorageConfig = storageConfig as SAFStorageConfig
    private val dataDir: SAFPath = this.storageRef.path.child("data")

    private val specAdapter = moshi.adapter(BackupSpec::class.java)
    private val metaDataAdapter = moshi.adapter(Backup.MetaData::class.java)
    private val propsAdapter = moshi.adapter(MMRef.Props::class.java)

    private val progressPub = HotData(Progress.Data())
    override val progress: Observable<Progress.Data> = progressPub.data

    private val dataDirEvents = Observable
            .create<List<SAFPath>> {
                try {
                    if (dataDir.exists(safGateway)) {
                        it.onNext(dataDir.listFiles(safGateway))
                    } else {
                        it.onNext(emptyList())
                    }
                    it.onComplete()
                } catch (e: IOException) {
                    it.tryOnError(e)
                }
            }
            .subscribeOn(Schedulers.io())
            .onErrorReturnItem(emptyList())
            .repeatWhen { it.delay(1, TimeUnit.SECONDS) }
            .filterUnchanged { old, new -> old != null && old == new }
            .replayingShare()

    init {
        Timber.tag(TAG).i("init(storageRef=%s, storageConfig=%s)", storageRef, storageConfig)
        if (!dataDir.exists(safGateway)) {
            Timber.tag(TAG).w("Data dir doesn't exist: %s", dataDir)
        }
    }

    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override fun info(): Observable<Storage.Info> = infoObs
    private val infoObs: Observable<Storage.Info> = Observable
            .fromCallable { Storage.Info(this.storageRef.storageId, this.storageRef.storageType, storageConfig) }
            .flatMap { info ->
                specInfos().map { contents ->
                    var status: Storage.Info.Status? = null
                    try {
                        status = Storage.Info.Status(
                                itemCount = contents.size,
                                totalSize = 0,
                                isReadOnly = dataDir.exists(safGateway) && !dataDir.canWrite(safGateway)
                        )
                    } catch (e: Exception) {
                        Timber.tag(TAG).w(e)
                    }
                    info.copy(status = status)
                }.startWith(info)
            }
            .doOnError { Timber.tag(TAG).e(it) }
            .doOnSubscribe { Timber.tag(TAG).d("info().doOnSubscribe()") }
            .doFinally { Timber.tag(TAG).d("info().doFinally()") }
            .onErrorMixLast { last, error ->
                // startWith
                last!!.copy(error = error)
            }
            .replayingShare()

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
                    if (backupDir.isFile(safGateway)) {
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

                    val ref = SAFStorageSpecInfo(
                            storageId = storageConfig.storageId,
                            path = backupDir,
                            backupSpec = backupConfig,
                            backups = metaDatas
                    )
                    content.add(ref)
                }
                return@map content.toList() as Collection<BackupSpec.Info>
            }
            .doOnError {
                if (it is InterruptedIOException) {
                    Timber.tag(TAG).w("specInfos().doOnError(): Interrupted")
                } else {
                    Timber.tag(TAG).e(it, "specInfos().doOnError()")
                }
            }
            .doOnSubscribe { Timber.tag(TAG).d("specInfos().doOnSubscribe()") }
            .doFinally { Timber.tag(TAG).d("specInfos().doFinally()") }

    override fun backupInfo(specId: BackupSpec.Id, backupId: Backup.Id): Observable<Backup.Info> = backupContent(specId, backupId)
            .map { Backup.Info(it.storageId, it.spec, it.metaData) }

    override fun backupContent(specId: BackupSpec.Id, backupId: Backup.Id): Observable<Backup.ContentInfo> = specInfo(specId)
            .flatMap { item ->
                item as SAFStorageSpecInfo
                val backupDir = getSpecDir(item.specId).requireExists(safGateway)
                val versionDir = backupDir.child(backupId.idString).requireExists(safGateway)
                val metaData = readBackupMeta(item.specId, backupId)
                val backupSpec = readSpec(item.specId)

                return@flatMap Observable.fromCallable { backupDir.listFiles(safGateway) }
                        .repeatWhen { it.delay(1, TimeUnit.SECONDS) }
                        .map {
                            return@map versionDir.listFiles(safGateway)
                                    .filter { it.name.endsWith(PROP_EXT) }
                                    .map { file ->
                                        val props = propsAdapter.fromSAFFile(safGateway, file)
                                        checkNotNull(props) { "Can't read props from $file" }
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
            .doOnError { Timber.tag(TAG).w(it, "Failed to get content: specId=$specId, backupId=$backupId") }
            .doFinally { Timber.tag(TAG).d("content(%s).doFinally()", backupId) }
            .replayingShare()

    override fun load(specId: BackupSpec.Id, backupId: Backup.Id): Backup.Unit {
        val item = specInfo(specId).blockingFirst()
        item as SAFStorageSpecInfo

        val metaData = readBackupMeta(specId, backupId)

        val dataMap = mutableMapOf<String, MutableList<MMRef>>()

        val versionPath = getVersionDir(specId, backupId).requireExists(safGateway)
        versionPath.listFiles(safGateway)
                .filter { it.name.endsWith(PROP_EXT) }
                .forEach { propFile ->
                    val prop = propsAdapter.fromSAFFile(safGateway, propFile)!!
                    val tmpRef = mmDataRepo.create(backupId, prop)

                    when (prop.refType) {
                        FILE -> {
                            val dataFile = versionPath.child(propFile.name.replace(PROP_EXT, DATA_EXT))
                            safGateway.openFile(dataFile, SAFGateway.FileMode.READ) { it.copyTo(tmpRef.tmpPath) }
                        }
                        DIRECTORY -> {
                            tmpRef.tmpPath.mkdirs()
                        }
                        MMRef.Type.UNUSED -> throw IllegalStateException("$prop is unused")
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
        updateProgressPrimary(storageConfig.label + ": " + context.getString(R.string.progress_saving_label))
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        try {
            val existingSpec = readSpec(backup.specId)
            check(existingSpec == backup.spec) { "BackupSpec missmatch:\nExisting: $existingSpec\n\nNew: ${backup.spec}" }
        } catch (e: Throwable) {
            Timber.tag(TAG).d("Reading existing spec failed (${e.message}, creating new one.")
            getSpecDir(backup.specId).tryMkDirs(safGateway)
            writeSpec(backup.specId, backup.spec)
        }

        val versionDir = getVersionDir(backup.specId, backup.backupId).tryMkDirs(safGateway)

        var current = 0
        val max = backup.data.values.fold(0, { cnt, vals -> cnt + vals.size })

        // TODO check that backup dir doesn't exist, ie version dir?
        backup.data.entries.forEach { (baseKey, refs) ->
            refs.forEach { ref ->
                updateProgressSecondary(ref.originalPath.path)
                updateProgressCount(Progress.Count.Counter(++current, max))

                var key = baseKey
                if (key.isNotBlank()) key += "#"

                val targetProp = versionDir.child("$key${ref.refId.idString}$PROP_EXT").requireNotExists(safGateway)
                propsAdapter.toSAFFile(ref.props, safGateway, targetProp)

                when (ref.type) {
                    FILE -> {
                        val target = versionDir.child("$key${ref.refId.idString}$DATA_EXT").requireNotExists(safGateway)
                        target.tryCreateFile(safGateway)
                        safGateway.openFile(target, SAFGateway.FileMode.WRITE) { ref.tmpPath.copyTo(it) }
                    }
                    DIRECTORY -> {
                        val target = versionDir.child("$key${ref.refId.idString}$DATA_EXT").requireNotExists(safGateway)
                        target.tryMkDirs(safGateway)
                    }
                    MMRef.Type.UNUSED -> throw IllegalStateException("$ref is unused")
                }
            }
        }

        writeBackupMeta(backup.specId, backup.backupId, backup.metaData)

        val info = Backup.Info(
                storageId = storageId,
                spec = backup.spec,
                metaData = backup.metaData
        )
        Timber.tag(TAG).d("New backup created: %s", info)
        return info
    }

    override fun remove(specId: BackupSpec.Id, backupId: Backup.Id?): Single<BackupSpec.Info> = specInfo(specId)
            .firstOrError()
            .map { specInfo ->
                specInfo as SAFStorageSpecInfo
                if (backupId != null) {
                    val versionDir = getVersionDir(specId, backupId)
                    versionDir.deleteAll(safGateway)
                } else {
                    val backupDir = getSpecDir(specId)
                    backupDir.deleteAll(safGateway)
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
                if (wipe) {
                    storageRef.path.asFile().deleteAll()
                }
                safGateway.releasePermission(storageRef.path)
            }
            .doOnSubscribe { Timber.w("detach(wipe=%b).doOnSubscribe %s", wipe, storageRef) }
            .doFinally { Timber.w("detach(wipe=%b).dofinally%s", wipe, storageRef) }

    private fun getSpecDir(specId: BackupSpec.Id): SAFPath = dataDir.child(specId.value)

    private fun readSpec(specId: BackupSpec.Id): BackupSpec {
        val specFile = getSpecDir(specId).child(SPEC_FILE)
        return try {
            specAdapter.fromSAFFile(safGateway, specFile)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to get backup spec from %s", specFile)
            throw e
        }
    }

    private fun writeSpec(specId: BackupSpec.Id, spec: BackupSpec) {
        val specFile = getSpecDir(specId).child(SPEC_FILE)
        specAdapter.toSAFFile(spec, safGateway, specFile)
    }

    private fun getVersionDir(specId: BackupSpec.Id, backupId: Backup.Id): SAFPath = getSpecDir(specId).child(backupId.idString)

    private fun getMetaDatas(specId: BackupSpec.Id): Collection<Backup.MetaData> {
        val metaDatas = mutableListOf<Backup.MetaData>()
        getSpecDir(specId).listFiles(safGateway).filter { it.isDirectory(safGateway) }.forEach { dir ->
            val metaData = readBackupMeta(specId, Backup.Id(dir.name))
            metaDatas.add(metaData)
        }
        return metaDatas
    }

    private fun readBackupMeta(specId: BackupSpec.Id, backupId: Backup.Id): Backup.MetaData {
        val versionFile = getVersionDir(specId, backupId).child(BACKUP_META_FILE)
        return metaDataAdapter.fromSAFFile(safGateway, versionFile)
    }

    private fun writeBackupMeta(specId: BackupSpec.Id, backupId: Backup.Id, metaData: Backup.MetaData) {
        val versionFile = getVersionDir(specId, backupId).child(BACKUP_META_FILE)
        metaDataAdapter.toSAFFile(metaData, safGateway, versionFile)
    }

    override fun toString(): String = "SAFStorage(storageConfig=$storageConfig)"

    @AssistedInject.Factory
    interface Factory : Storage.Factory<SAFStorage>

    companion object {
        val TAG = App.logTag("StorageRepo", "SAF")
        private const val DATA_EXT = ".data"
        private const val PROP_EXT = ".prop.json"
        private const val SPEC_FILE = "spec.json"
        private const val BACKUP_META_FILE = "backup.json"
    }

}