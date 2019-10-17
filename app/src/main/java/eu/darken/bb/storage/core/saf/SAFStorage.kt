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
import eu.darken.bb.common.file.*
import eu.darken.bb.common.moshi.fromSAFFile
import eu.darken.bb.common.moshi.toSAFFile
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.rx.filterUnchanged
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.MMRef.Type.DIRECTORY
import eu.darken.bb.processor.core.mm.MMRef.Type.FILE
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.local.LocalStorage
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
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
            .fromCallable { dataDir.listFiles(safGateway) }
            .subscribeOn(Schedulers.io())
            .onErrorReturnItem(emptyArray())
            .repeatWhen { it.delay(1, TimeUnit.SECONDS) }
            .filterUnchanged { old, new -> old != null && old.contentEquals(new) }
            .replayingShare()

    init {
        Timber.tag(TAG).i("init(storageRef=%s, storageConfig=%s)", storageRef, storageConfig)
        if (!dataDir.exists(safGateway)) {
            Timber.tag(TAG).w("Data dir doesn't exist: %s", dataDir)
        }
    }

    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressPub.update(update)

    override fun items(vararg specIds: BackupSpec.Id): Observable<Collection<BackupSpec.Info>> = items()
            .map { specInfos ->
                specIds.map { specId ->
                    val item = specInfos.find { it.backupSpec.specId == specId }
                    requireNotNull(item) { "Can't find backup item for specId $specId" }
                }
            }

    override fun items(): Observable<Collection<BackupSpec.Info>> = itemObs
    private val itemObs: Observable<Collection<BackupSpec.Info>> = dataDirEvents
            .map { files ->
                val content = mutableListOf<BackupSpec.Info>()

                for (backupDir in files) {
                    if (backupDir.isFile(safGateway)) {
                        Timber.tag(TAG).w("Unexpected file within data directory: %s", backupDir)
                        continue
                    }

                    val backupConfig = try {
                        specAdapter.fromSAFFile(safGateway, backupDir.child(SPEC_FILE))
                    } catch (e: Exception) {
                        Timber.tag(TAG).w(e, "Failed to read specfile")
                        null
                    }
                    if (backupConfig == null) {
                        Timber.tag(TAG).w("Dir without spec file: %s", backupDir)
                        continue
                    }

                    val metaDatas = getMetaDatas(backupConfig.specId)
                    if (metaDatas.isEmpty()) {
                        Timber.tag(LocalStorage.TAG).w("Dir without backups? %s", backupDir)
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
            .doOnError { Timber.tag(TAG).e(it) }
            .doOnSubscribe { Timber.tag(TAG).d("doOnSubscribe().doFinally()") }
            .doFinally { Timber.tag(TAG).d("items().doFinally()") }

    override fun info(): Observable<Storage.Info> = infoObs
    private val infoObs: Observable<Storage.Info> = items()
            .map { contents ->
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

    override fun content(specId: BackupSpec.Id, backupId: Backup.Id): Observable<Backup.Info> = items(specId)
            .map { it.first() }
            .flatMap { item ->
                item as SAFStorageSpecInfo
                val backupDir = getSpecDir(item.specId).requireExists(safGateway)
                val versionDir = backupDir.child(backupId.idString).requireExists(safGateway)

                val metaData = readBackupMeta(item.specId, backupId)
                checkNotNull(metaData) { "Can't read metadata file in $versionDir" }

                val backupSpec = specAdapter.fromSAFFile(safGateway, backupDir.child(SPEC_FILE))
                checkNotNull(backupSpec) { "Can't read $backupDir" }

                return@flatMap Observable.fromCallable { backupDir.listFiles(safGateway) }
                        .repeatWhen { it.delay(1, TimeUnit.SECONDS) }
                        .map {
                            return@map versionDir.listFiles(safGateway)
                                    .filter { it.name.endsWith(PROP_EXT) }
                                    .map { file ->
                                        val props = propsAdapter.fromSAFFile(safGateway, file)
                                        checkNotNull(props) { "Can't read props from $file" }
                                        Backup.Info.PropsEntry(backupSpec, metaData, props)
                                    }
                                    .toList()
                        }
                        .onErrorReturnItem(emptyList())
                        .map {
                            return@map Backup.Info(
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
        val item = items(specId).map { it.first() }.blockingFirst()
        item as SAFStorageSpecInfo

        val metaData = readBackupMeta(specId, backupId)
        requireNotNull(metaData) { "BackupReference $item does not have a metadata file" }

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
        updateProgressPrimary(storageConfig.label + ": " + context.getString(R.string.progress_label_saving))
        updateProgressSecondary("")
        updateProgressCount(Progress.Count.Indeterminate())

        val backupDir = getSpecDir(backup.specId).tryMkDirs(safGateway)

        val specFile = backupDir.child(SPEC_FILE)
        if (!specFile.exists(safGateway)) {
            specAdapter.toSAFFile(backup.spec, safGateway, specFile)
        } else {
            val existingSpec = specAdapter.fromSAFFile(safGateway, specFile)
            check(existingSpec == backup.spec) { "BackupSpec missmatch:\nExisting: $existingSpec\n\nNew: ${backup.spec}" }
        }

        val versionDir = getVersionDir(backup.specId, backup.backupId).tryMkDirs(safGateway)

        var current = 0
        val max = backup.data.values.fold(0, { cnt, vals -> cnt + vals.size })

        // TODO check that backup dir doesn't exist, ie version dir?
        val itemEntries = mutableListOf<Backup.Info.Entry>()
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
                itemEntries.add(Backup.Info.PropsEntry(backup.spec, backup.metaData, ref.props))
            }
        }

        writeBackupMeta(backup.specId, backup.backupId, backup.metaData)

        val info = Backup.Info(
                storageId = storageId,
                spec = backup.spec,
                metaData = backup.metaData,
                items = itemEntries
        )
        Timber.tag(TAG).d("New backup created: %s", info)
        return info
    }

    override fun remove(specId: BackupSpec.Id, backupId: Backup.Id?): Single<BackupSpec.Info> = items(specId)
            .firstOrError()
            .map { it.first() as SAFStorageSpecInfo }
            .map { specInfo ->
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

    // TODO remove URI Permission
    override fun detach(): Completable = Completable
            .complete()
            .doOnSubscribe { Timber.i("Detaching %s", storageRef) }

    // TODO call detach after wipe?
    override fun wipe(): Completable = Completable
            .fromCallable {
                storageRef.path.asFile().deleteAll()
            }
            .doOnSubscribe { Timber.w("wipe().doOnSubscribe %s", storageRef) }
            .doFinally { Timber.w("wipe().dofinally%s", storageRef) }

    private fun getSpecDir(specId: BackupSpec.Id): SAFPath = dataDir.child(specId.value)

    private fun getVersionDir(specId: BackupSpec.Id, backupId: Backup.Id): SAFPath = getSpecDir(specId).child(backupId.idString)

    private fun getMetaDatas(specId: BackupSpec.Id): Collection<Backup.MetaData> {
        val metaDatas = mutableListOf<Backup.MetaData>()
        getSpecDir(specId).listFiles(safGateway).filter { it.isDirectory(safGateway) }.forEach { dir ->
            val metaData = readBackupMeta(specId, Backup.Id(dir.name))
            if (metaData != null) {
                metaDatas.add(metaData)
            } else {
                Timber.tag(LocalStorage.TAG).w("Version dir without metadata: %s", dir)
            }
        }
        return metaDatas
    }

    private fun readBackupMeta(specId: BackupSpec.Id, backupId: Backup.Id): Backup.MetaData? {
        val versionFile = getVersionDir(specId, backupId).child(BACKUP_META_FILE)
        return try {
            metaDataAdapter.fromSAFFile(safGateway, versionFile)
        } catch (e: Exception) {
            Timber.tag(LocalStorage.TAG).w(e, "Failed to get metadata from ", versionFile)
            null
        }
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