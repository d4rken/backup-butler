package eu.darken.bb.storage.core.common

import com.squareup.moshi.Moshi
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.*
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.Logging.Priority.*
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.error.hasCause
import eu.darken.bb.common.files.core.*
import eu.darken.bb.common.files.core.local.*
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.flow.onError
import eu.darken.bb.common.flow.onErrorMixLast
import eu.darken.bb.common.flow.replayingShare
import eu.darken.bb.common.moshi.fromAPath
import eu.darken.bb.common.moshi.toAPath
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.sharedresource.SharedResource
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.MMRef.Type.*
import eu.darken.bb.processor.core.mm.archive.ArchiveProps
import eu.darken.bb.processor.core.mm.archive.ArchiveRefSource
import eu.darken.bb.processor.core.mm.generic.GenericRefSource
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.local.LocalStorageSpecInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.InterruptedIOException
import kotlin.time.Duration


abstract class CommonStorage<
    P : APath,
    PL : APathLookup<P>,
    GW : APathGateway<P, PL>
    > constructor(
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    moshi: Moshi,
    private val mmDataRepo: MMDataRepo,
    private val gateway: GW,
    val tag: String,
    refreshTrigger: Flow<Unit> = TIME_TRIGGER,
    val storageRef: Storage.Ref,
    override val storageConfig: Storage.Config,
) : Storage, HasContext, Progress.Client {


    override val sharedResource = SharedResource.createKeepAlive(
        "$tag:SR",
        appScope + dispatcherProvider.IO
    )

    abstract val dataDir: P

    private val specAdapter = moshi.adapter(BackupSpec::class.java)
    private val metaDataAdapter = moshi.adapter(Backup.MetaData::class.java)

    private val progressPub = DynamicStateFlow(tag, appScope) { Progress.Data() }
    override val progress: Flow<Progress.Data> = progressPub.flow
    override fun updateProgress(update: suspend (Progress.Data) -> Progress.Data) = progressPub.updateAsync(onUpdate = update)

    init {
        log(tag) { "init(storageRef=$storageRef, storageConfig=$storageConfig)" }
    }

    private suspend fun <T> runStorageOp(
        block: suspend CoroutineScope.() -> T
    ): T = withContext(dispatcherProvider.Default) {
        gateway.sharedResource.get().use { block() }
    }

    private val dataDirEvents: Flow<List<PL>> = refreshTrigger
        .onEach { log(tag, VERBOSE) { "refresh triggered." } }
        .map { dataDir.lookupFilesOrNull(gateway) ?: emptyList() }
        .distinctUntilChanged()
        .replayingShare(appScope)

    override fun info(): Flow<Storage.Info> = infowFlow
    private val infowFlow: Flow<Storage.Info> = flow<Storage.Info> {
        val info = Storage.Info(storageRef.storageId, storageRef.storageType, storageConfig)
        emit(info)

        var status = Storage.Info.Status(
            isReadOnly = dataDir.exists(gateway) && !dataDir.canWrite(gateway),
            itemCount = -1,
            totalSize = -1L
        )
        emit(info.copy(status = status))

        status = status.copy(
            itemCount = dataDir.listFilesOrNull(gateway)?.size ?: 0
        )
        emit(info.copy(status = status))

        status = status.copy(
            totalSize = if (status.itemCount > 0) {
                dataDir.walk(gateway).fold(0L) { totalSize, file -> totalSize + file.size }
            } else {
                0L
            }
        )
        emit(info.copy(status = status))
    }
        .onError { Timber.tag(tag).e(it) }
        .onStart { Timber.tag(tag).d("info().onStart()") }
        .onCompletion { Timber.tag(tag).d("info().onCompletion()") }
        .onErrorMixLast { last, error ->
            // First emit(info) needs to be error free
            requireNotNull(last)
            if (error.hasCause(CancellationException::class)) {
                log(tag, DEBUG) { "infoFlow was cancelled." }
                last
            } else {
                last.copy(error = error)
            }
        }
        .shareIn(
            scope = appScope,
            replay = 1,
            started = SharingStarted.WhileSubscribed(replayExpiration = Duration.seconds(10))
        )

    override fun specInfo(specId: BackupSpec.Id): Flow<BackupSpec.Info> = specInfos()
        .map { specInfos ->
            val item = specInfos.find { it.backupSpec.specId == specId }
            requireNotNull(item) { "Can't find backup item for specId $specId" }
        }

    override fun specInfos(): Flow<Collection<BackupSpec.Info>> = itemObs
    private val itemObs: Flow<Collection<BackupSpec.Info>> = dataDirEvents
        .map { files ->
            val content = mutableListOf<BackupSpec.Info>()

            for (backupDir in files) {
                if (backupDir.isFile) {
                    Timber.tag(tag).w("Unexpected file within data directory: %s", backupDir)
                    continue
                }

                val backupConfig = try {
                    readSpec(BackupSpec.Id(backupDir.name))
                } catch (e: Throwable) {
                    if (e is InterruptedIOException) throw e
                    Timber.tag(tag).w("Dir without spec file: %s", backupDir)
                    continue
                }

                val metaDatas = getMetaDatas(backupConfig.specId)
                if (metaDatas.isEmpty()) {
                    Timber.tag(tag).w("Dir without backups? %s", backupDir)
                    continue
                }

                val ref = CommonStorageSpecInfo(
                    storageId = storageConfig.storageId,
                    path = backupDir.lookedUp,
                    backupSpec = backupConfig,
                    backups = metaDatas
                )
                content.add(ref)
            }
            content
        }
        .map { it }
        .onError { log(tag, ERROR) { "specInfos() failed: ${it.asLog()}" } }
        .onStart { Timber.tag(tag).d("doOnSubscribe().doFinally()") }
        .onCompletion { Timber.tag(tag).d("specInfos().doFinally()") }
        .replayingShare(appScope)

    override fun backupInfo(specId: BackupSpec.Id, backupId: Backup.Id): Flow<Backup.Info> =
        backupContent(specId, backupId)
            .map { Backup.Info(it.storageId, it.spec, it.metaData) }

    override fun backupContent(specId: BackupSpec.Id, backupId: Backup.Id): Flow<Backup.ContentInfo> =
        specInfo(specId)
            .flatMapConcat { item ->

                val backupDir = (item.path as P).requireExists(gateway)
                val versionDir = (backupDir.child(backupId.idString) as P).requireExists(gateway)
                val backupSpec = readSpec(specId)
                val metaData = readBackupMeta(item.specId, backupId)

                return@flatMapConcat flow<List<P>> {

                    while (true) {
                        emit(backupDir.listFiles(gateway))
                        delay(1000L)
                        yield()
                    }
                }
                    .distinctUntilChanged()
                    .map {
                        return@map versionDir.listFiles(gateway)
                            .filter { it.path.endsWith(PROP_EXT) }
                            .map { file ->
                                val props = file.read(gateway).use { mmDataRepo.readProps(it) }
                                Backup.ContentInfo.PropsEntry(backupSpec, metaData, props)
                            }
                            .toList()
                    }
                    .catch { emit(emptyList()) }
                    .map {
                        return@map Backup.ContentInfo(
                            storageId = storageId,
                            spec = backupSpec,
                            metaData = metaData,
                            items = it
                        )
                    }
            }
            .onStart { Timber.tag(tag).d("content(%s).doOnSubscribe()", backupId) }
            .onError { log(tag, WARN) { "Failed to get content: specId=$specId, backupId=$backupId" } }
            .onCompletion { Timber.tag(tag).d("content(%s).doFinally()", backupId) }
            .replayingShare(appScope)

    override suspend fun load(specId: BackupSpec.Id, backupId: Backup.Id): Backup.Unit = runStorageOp {
        log(tag) { "load(specId=$specId, backupId=$backupId)" }
        updateProgressPrimary(R.string.progress_reading_backup_specs)
        updateProgressCount(Progress.Count.Indeterminate())
        val item = specInfo(specId).first()
        item as LocalStorageSpecInfo

        updateProgressPrimary(R.string.progress_reading_backup_metadata)
        val metaData = readBackupMeta(specId, backupId)

        updateProgressPrimary(R.string.progress_reading_backup_data)
        val dataMap = mutableMapOf<String, MutableList<MMRef>>()
        val versionPath = getVersionDir(specId, backupId).requireExists(gateway)
        val propFiles = versionPath.listFiles(gateway).filter { it.path.endsWith(PROP_EXT) }
        updateProgressCount(Progress.Count.Percent(0, propFiles.size))

        propFiles.forEachIndexed { index, propFile ->
            updateProgressSecondary(propFile.path)

            val dataFile = versionPath.childCast(propFile.name.replace(PROP_EXT, DATA_EXT))
            val props = propFile.read(gateway).use { mmDataRepo.readProps(it) }

            val source: MMRef.RefSource = when (props.dataType) {
                FILE, DIRECTORY, SYMLINK -> GenericRefSource({ dataFile.read(gateway) }, { props })
                ARCHIVE -> ArchiveRefSource({ dataFile.read(gateway) }, { props as ArchiveProps })
            }

            val tmpRef = mmDataRepo.create(MMRef.Request(backupId = backupId, source = source))

            val keySplit = propFile.name.split("#")
            val key = if (keySplit.size == 2) Base64Tool.decode(keySplit[0]) else ""
            dataMap.getOrPut(key, { mutableListOf() }).add(tmpRef)

            updateProgressCount(Progress.Count.Percent(index + 1, propFiles.size))
        }
        updateProgressSecondary(CaString.EMPTY)
        updateProgressCount(Progress.Count.Indeterminate())

        Backup.Unit(
            spec = item.backupSpec,
            metaData = metaData,
            data = dataMap
        )
    }

    override suspend fun save(backup: Backup.Unit): Backup.Info = runStorageOp {
        log(tag) { "save(backup=$backup)" }
        updateProgressPrimary(R.string.progress_writing_backup_specs)
        updateProgressCount(Progress.Count.Indeterminate())
        try {
            val existingSpec = readSpec(backup.specId)
            check(existingSpec == backup.spec) {
                "BackupSpec missmatch:\nExisting: $existingSpec\n\nNew: ${backup.spec}"
            }
        } catch (e: Exception) {
            log(tag) { "Reading existing spec failed, creating new one: ${e.message}" }
            getSpecDir(backup.specId).tryMkDirs(gateway)
            writeSpec(backup.specId, backup.spec)
        }

        updateProgressPrimary(R.string.progress_writing_backup_data)
        val versionDir = getVersionDir(specId = backup.specId, backupId = backup.backupId)
        versionDir.tryMkDirs(gateway)

        var current = 0
        val max = backup.data.values.fold(0, { cnt, vals -> cnt + vals.size })
        updateProgressCount(Progress.Count.Counter(current, max))

        // TODO guardAction that backup dir doesn't exist, ie version dir?

        backup.data.entries.forEach { (baseKey, refs) ->
            refs.forEach { ref ->

                val key = if (baseKey.isNotBlank()) {
                    val encoded = Base64Tool.encode(baseKey)
                    "$encoded#"
                } else {
                    ""
                }

                val targetProp =
                    versionDir.childCast("$key${ref.refId.idString}$PROP_EXT").requireNotExists(gateway)
                updateProgressSecondary(targetProp.path)
                targetProp.write(gateway).use { mmDataRepo.writeProps(ref.getProps(), it) }

                // TODO errors should be shown in the result?
                when (ref.getProps().dataType) {
                    FILE, ARCHIVE -> {
                        val target =
                            versionDir.childCast("$key${ref.refId.idString}$DATA_EXT").requireNotExists(gateway)
                        ref.source.open().copyToAutoClose(target.write(gateway))
                    }
                    DIRECTORY, SYMLINK -> {
                        // NOOP props are enough
                    }
                }

                updateProgressCount(Progress.Count.Counter(++current, max))
            }
        }
        updateProgressSecondary(CaString.EMPTY)
        updateProgressCount(Progress.Count.Indeterminate())

        updateProgressPrimary(R.string.progress_writing_backup_metadata)
        writeBackupMeta(backup.specId, backup.backupId, backup.metaData)
        val info = Backup.Info(
            storageId = storageId,
            spec = backup.spec,
            metaData = backup.metaData
        )
        Timber.tag(tag).d("New backup created: %s", info)
        info
    }

    override suspend fun remove(specId: BackupSpec.Id, backupId: Backup.Id?): BackupSpec.Info = runStorageOp {
        log(tag) { "remove(specId=$specId, backupId=$backupId)" }
        val specInfo = specInfo(specId).first()
        specInfo as LocalStorageSpecInfo
        if (backupId != null) {
            val versionDir = getVersionDir(specId, backupId)
            versionDir.deleteAll(gateway)
        } else {
            val backupDir = getSpecDir(specId)
            backupDir.deleteAll(gateway)
        }
        val newMetaData = if (backupId != null) {
            // Not a complete deletion
            getMetaDatas(specId)
        } else {
            emptySet()
        }
        specInfo.copy(backups = newMetaData)
    }

    override suspend fun detach(wipe: Boolean) = runStorageOp {
        log(tag) { "detach(wipe=$wipe)" }
        Timber.tag(tag).d("detach(wipe=%b): %s", wipe, storageRef)
        if (wipe) {
            Timber.tag(tag).i("Wiping %s", storageRef.path)
            // let's not use the gateway here as there is currently no reason to run this with root.
            (storageRef.path as P).deleteAll(gateway)
        }
        // TODO dispose resources, i.e. observables.
        Timber.tag(tag).d("detach(wipe=%b): %s", wipe, storageRef)
    }


    private fun getSpecDir(specId: BackupSpec.Id): P = dataDir.childCast(specId.value)

    private fun getVersionDir(specId: BackupSpec.Id, backupId: Backup.Id): P =
        getSpecDir(specId).childCast(backupId.idString)

    private suspend fun getMetaDatas(specId: BackupSpec.Id): Collection<Backup.MetaData> {
        val metaDatas = mutableListOf<Backup.MetaData>()
        getSpecDir(specId).lookupFiles(gateway).filter { it.isDirectory }.forEach { dir ->
            try {
                val metaData = readBackupMeta(specId, Backup.Id(dir.name))
                metaDatas.add(metaData)
            } catch (e: ReadException) {
                // TODO How will these get cleaned up?
                log(tag, ERROR) { "Failed to read meta data for $dir: ${e.asLog()}" }
            }
        }
        return metaDatas
    }

    private suspend fun readBackupMeta(specId: BackupSpec.Id, backupId: Backup.Id): Backup.MetaData {
        val versionFile = getVersionDir(specId, backupId).childCast(BACKUP_META_FILE)
        return try {
            metaDataAdapter.fromAPath(gateway, versionFile)
        } catch (e: Exception) {
            log(tag, WARN) { "Failed to get metadata from $versionFile: ${e.asLog()}" }
            throw e
        }
    }

    private suspend fun writeBackupMeta(specId: BackupSpec.Id, backupId: Backup.Id, metaData: Backup.MetaData) {
        val versionFile = getVersionDir(specId, backupId).childCast(BACKUP_META_FILE)
        metaDataAdapter.toAPath(metaData, gateway, versionFile)
    }

    private suspend fun readSpec(specId: BackupSpec.Id): BackupSpec {
        val specFile = getSpecDir(specId).childCast(SPEC_FILE)
        return try {
            specAdapter.fromAPath(gateway, specFile)
        } catch (e: Exception) {
            log(tag, WARN) { "Failed to get backup spec from $specFile: ${e.asLog()}" }
            throw e
        }
    }

    private suspend fun writeSpec(specId: BackupSpec.Id, spec: BackupSpec) {
        val specFile = getSpecDir(specId).childCast(SPEC_FILE)
        specAdapter.toAPath(spec, gateway, specFile)
    }

    override fun toString(): String = "Storage(ref=$storageRef, config=$storageConfig)"

    companion object {
        private const val DATA_EXT = ".data"
        private const val PROP_EXT = ".prop.json"
        private const val SPEC_FILE = "spec.json"
        private const val BACKUP_META_FILE = "backup.json"

        // TODO Improve this
        private val TIME_TRIGGER = flow<Unit> {
            while (true) {
                emit(Unit)
                delay(3000)
                yield()
            }
        }
    }
}